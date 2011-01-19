package org.sonatype.aether.connector.async;

import org.sonatype.aether.repository.RemoteRepository;

/**
 * @author Benjamin Hanzelmann
 *
 */
public class DavUrlPutTest
    extends PutTest
{

    @Override
    protected RemoteRepository repository()
    {
        RemoteRepository repo = super.repository();
        return repo.setUrl( "dav:" + repo.getUrl() );
    }

}
