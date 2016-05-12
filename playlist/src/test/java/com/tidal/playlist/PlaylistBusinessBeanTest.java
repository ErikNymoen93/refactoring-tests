package com.tidal.playlist;

import com.google.inject.Inject;
import com.tidal.playlist.data.PlayListTrack;
import com.tidal.playlist.data.Track;
import com.tidal.playlist.dao.PlaylistDaoBean;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.testng.AssertJUnit.assertTrue;

/**
 * @author: eivind.hognestad@wimpmusic.com
 * Date: 15.04.15
 * Time: 14.32
 */
@Guice(modules = TestBusinessModule.class)
public class PlaylistBusinessBeanTest {
	
	private PlaylistDaoBean playlistDaoBean;

	@Inject
	    public PlaylistBusinessBeanTest(PlaylistDaoBean playlistDaoBean){
	        this.playlistDaoBean = playlistDaoBean;
	    }

    @Inject
    PlaylistBusinessBean playlistBusinessBean;

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test
    public void testAddAndRemoveTracks() throws Exception {
        List<Track> trackList = new ArrayList<Track>();

        Track track = new Track();
        track.setArtistId(4);
        track.setTitle("A brand new track");
        track.setTrackNumberIdx(1);
        track.setId(76868);

        trackList.add(track);
        
        //Adding a new track
        track = new Track();
        track.setArtistId(6);
        track.setTitle("Another brand new track");
        track.setTrackNumberIdx(2);
        track.setId(15371);

        trackList.add(track);
        
        //Creating uuid string
        String uuid = UUID.randomUUID().toString();

        List<PlayListTrack> playListTracks = playlistBusinessBean.addTracks(uuid, 1, trackList, 5, new Date());

        
        assertTrue(playListTracks.size() > 0);  //Checks if there are any tracks in the list
        assertTrue(playListTracks.size() == 2); //Checks if there are 2 tracks in the list (too see if the new one is added)
        
        TrackPlayList playList = playlistDaoBean.getPlaylistByUUID(uuid, 1);
        
        List<PlayListTrack> tracksToDelete = new ArrayList<PlayListTrack>();
        
        //Deleting track by it's Id (15371)
        tracksToDelete.add(playList.getPlayListTrack(15371));
        
        playlistBusinessBean.deleteTracks(uuid, 1, tracksToDelete);
        
        assertTrue(playList.getPlayListTracksSize() < 2); //checks if there are less than two tracks in the list, which it should be if the track was deleted
        

        
    }
    
}
