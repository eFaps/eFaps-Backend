package org.efaps.backend.cluster;

import org.eclipse.microprofile.config.Config;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupCommunication
{

    private static final Logger LOG = LoggerFactory.getLogger(GroupCommunication.class);

    private static JChannel CHANNEL;

    public void init(final Config config)
    {
        final var fileOpt = config.getOptionalValue("backend.jGroups.file", String.class);
        if (fileOpt.isPresent()) {
            final var file = fileOpt.get();
            LOG.info("Got JGroupsFile: {}", file);

            try {
                final var channel = new JChannel(file);
                channel.setReceiver(new Receiver()
                {

                    @Override
                    public void receive(final Message msg)
                    {
                        LOG.info("received message: {}", msg);
                    }

                    @Override
                    public void viewAccepted(final View view)
                    {
                        LOG.info("received view: {}", view);
                    }
                });

                final var clusterName = config.getValue("backend.jGroups.clusterName", String.class);
                channel.connect(clusterName);
                CHANNEL = channel;
            } catch (final Exception e) {
                LOG.error("Catched", e);
            }

        } else {
            LOG.debug("No JGroupsFile");
        }
    }

    public static JChannel getChannel()
    {
        return CHANNEL;
    }
}
