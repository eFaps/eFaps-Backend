/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.efaps.backend.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.AppConfigHandler;
import org.efaps.admin.user.Company;
import org.efaps.backend.errors.InvalidSignatureException;
import org.efaps.backend.injection.Anonymous;
import org.efaps.backend.injection.NoContext;
import org.efaps.db.Context;
import org.efaps.db.Context.Inheritance;
import org.efaps.util.EFapsException;
import org.efaps.util.SignUtil;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("signed-upload")
@NoContext
@Anonymous
public class SignedUploadResource
{
    private static final Logger LOG = LoggerFactory.getLogger(SignedUploadResource.class);

    @POST
    public Response upload(@QueryParam("expires") final long expires,
                           @QueryParam("ctxU") final UUID userUuid,
                           @QueryParam("ctxC") final UUID companyUuid,
                           @QueryParam("ref") final String ref,
                           @QueryParam("signature") final String signature,
                           final FormDataMultiPart formData)
        throws InvalidSignatureException
    {
        final Response response = null;
        if (userUuid == null || companyUuid == null || signature == null) {
            throw new InvalidSignatureException("Missing query parameter");
        }

        final boolean isExpired = Instant.now().getEpochSecond() > expires;
        if (isExpired) {
            throw new InvalidSignatureException("Expired");
        }
        String correctSignature;
        try {
            correctSignature = ref == null ? SignUtil.sign(expires, userUuid, companyUuid)
                            : SignUtil.sign(expires, userUuid, companyUuid, ref);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new InvalidSignatureException("Invalid key");
        }
        if (!correctSignature.equals(signature)) {
            throw new InvalidSignatureException("Invalid signature");
        }
        try {
            Context.begin(userUuid.toString(), Inheritance.Inheritable);
            Context.getThreadContext().setRequestAttribute("REST", true);
            final var company = Company.get(companyUuid);
            Context.getThreadContext().setCompany(company);
            MDC.put("company", String.format("'%s' (%s)", company.getUUID(), company.getName()));
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
            throw new InvalidSignatureException("Invalid Context");
        }
        try {
            final var filePart = formData.getField("file");
            if (filePart == null) {
                System.out.println("no content");
            } else {
                final var fileName = filePart.getContentDisposition().getFileName();
                final var file = getFile(fileName);
                final InputStream is = filePart.getContent();
                FileUtils.copyInputStreamToFile(is, file);

                final var clazz = Class.forName("org.efaps.esjp.common.file.SignedUrl");
                final Object signedUrl = clazz.getDeclaredConstructor().newInstance();
                final var method = clazz.getMethod("onUpload", File.class, String.class);
                final var responseObj = method.invoke(signedUrl, file, ref);
                if (responseObj != null) {
                    return Response.ok(responseObj).build();
                }
            }
        } catch (final EFapsException | IOException | ClassNotFoundException | InstantiationException
                        | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
            LOG.error("Catched", e);
            throw new InvalidSignatureException("Invalid Context");
        }
        return response;
    }

    public File getFile(final String fileName)
        throws EFapsException, IOException
    {
        File tmpfld = AppConfigHandler.get().getTempFolder();
        if (tmpfld == null) {
            final File temp = File.createTempFile("eFaps", ".tmp");
            tmpfld = temp.getParentFile();
            temp.delete();
        }
        final File storeFolder = new File(tmpfld, "eFapsSignedUploads");
        final NumberFormat formater = NumberFormat.getInstance();
        formater.setMinimumIntegerDigits(8);
        formater.setGroupingUsed(false);
        final File userFolder = new File(storeFolder, formater.format(Context.getThreadContext().getPersonId()));
        if (!userFolder.exists()) {
            userFolder.mkdirs();
        }
        final String name = StringUtils.stripAccents(fileName);
        return new File(userFolder, name.replaceAll("[^a-zA-Z0-9.-]", "_"));
    }
}
