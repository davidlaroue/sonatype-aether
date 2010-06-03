package org.sonatype.maven.repository.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.maven.repository.MergeableMetadata;
import org.sonatype.maven.repository.RepositoryException;
import org.sonatype.maven.repository.internal.metadata.Metadata;
import org.sonatype.maven.repository.internal.metadata.Versioning;
import org.sonatype.maven.repository.internal.metadata.io.xpp3.MetadataXpp3Reader;
import org.sonatype.maven.repository.internal.metadata.io.xpp3.MetadataXpp3Writer;

/**
 * @author Benjamin Bentmann
 */
abstract class MavenMetadata
    implements MergeableMetadata
{

    private File file;

    protected Metadata metadata;

    public String getType()
    {
        return "maven-metadata.xml";
    }

    public File getFile()
    {
        return file;
    }

    public void setFile( File file )
    {
        this.file = file;
    }

    public void merge( File existing, File result )
        throws RepositoryException
    {
        Metadata recessive = read( existing );

        merge( recessive );

        write( result, metadata );
    }

    protected void merge( Metadata recessive )
    {
        Versioning versioning = recessive.getVersioning();
        if ( versioning != null )
        {
            versioning.setLastUpdated( null );
        }

        Metadata dominant = metadata;

        versioning = dominant.getVersioning();
        if ( versioning != null )
        {
            versioning.updateTimestamp();
        }

        dominant.merge( recessive );
    }

    private Metadata read( File metadataFile )
        throws RepositoryException
    {
        if ( metadataFile.length() <= 0 )
        {
            return new Metadata();
        }

        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( metadataFile );
            return new MetadataXpp3Reader().read( reader, false );
        }
        catch ( IOException e )
        {
            throw new RepositoryException( "Could not read metadata " + metadataFile + ": " + e.getMessage(), e );
        }
        catch ( XmlPullParserException e )
        {
            throw new RepositoryException( "Could not parse metadata " + metadataFile + ": " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    private void write( File metadataFile, Metadata metadata )
        throws RepositoryException
    {
        Writer writer = null;
        try
        {
            metadataFile.getParentFile().mkdirs();
            writer = WriterFactory.newXmlWriter( metadataFile );
            new MetadataXpp3Writer().write( writer, metadata );
        }
        catch ( IOException e )
        {
            throw new RepositoryException( "Could not write metadata " + metadataFile + ": " + e.getMessage(), e );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder( 128 );
        if ( getGroupId().length() > 0 )
        {
            buffer.append( getGroupId() );
        }
        if ( getArtifactId().length() > 0 )
        {
            buffer.append( ':' ).append( getArtifactId() );
        }
        if ( getVersion().length() > 0 )
        {
            buffer.append( ':' ).append( getVersion() );
        }
        buffer.append( '/' ).append( getType() );
        return buffer.toString();
    }

}
