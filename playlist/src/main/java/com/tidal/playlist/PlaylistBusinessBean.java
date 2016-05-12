package com.tidal.playlist;

import com.google.inject.Inject;
import com.tidal.playlist.dao.PlaylistDaoBean;
import com.tidal.playlist.data.PlayListTrack;
import com.tidal.playlist.data.Track;
import com.tidal.playlist.data.TrackPlayList;
import com.tidal.playlist.exception.PlaylistException;

import java.util.*;
/**
 * @author: eivind.hognestad@wimpmusic.com
 * Date: 15.04.15
 * Time: 12.45
 */
public class PlaylistBusinessBean {

    private PlaylistDaoBean playlistDaoBean;

    @Inject
    public PlaylistBusinessBean(PlaylistDaoBean playlistDaoBean){
        this.playlistDaoBean = playlistDaoBean;
    }

    List<PlayListTrack> addTracks(String uuid, int userId, List<Track> tracksToAdd, int sizeIndex,
                                  Date lastUpdated) throws PlaylistException {

        try {

            TrackPlayList playList = playlistDaoBean.getPlaylistByUUID(uuid, userId);

            //We do not allow > 500 tracks in new playlists
            int nrOfTracks = playList.getNrOfTracks(); //Extract local variable
			if (nrOfTracks + tracksToAdd.size() > 500) {
                throw new PlaylistException("Playlist cannot have more than " + 500 + " tracks");
            }

            // The index is out of bounds, put it in the end of the list.
            int playListTracksSize = playList.getPlayListTracksSize(); //Extract local variable
			if (sizeIndex > playListTracksSize || sizeIndex == -1) {
                sizeIndex = playListTracksSize;
            }

            if (!validateIndexes(sizeIndex, nrOfTracks)) {
                return Collections.EMPTY_LIST;
            }

            Set<PlayListTrack> playListTracks = playList.getPlayListTracks();
			Set<PlayListTrack> originalSet = playListTracks;
            List<PlayListTrack> original;
            if (originalSet == null || originalSet.size() == 0)
                original = new ArrayList<PlayListTrack>();
            else
                original = new ArrayList<PlayListTrack>(originalSet);

            Collections.sort(original);

            List<PlayListTrack> addedList = new ArrayList<PlayListTrack>(tracksToAdd.size());

            // Extract method
            setTrackDetails(tracksToAdd, sizeIndex, lastUpdated, playList, original, addedList);

            int i = 0;
            for (PlayListTrack track : original) {
                track.setIndex(i++);
            }

            playListTracks.clear();
            playListTracks.addAll(original);
            playList.setNrOfTracks(original.size());

            return addedList;

        } catch (Exception e) {
            e.printStackTrace();
            throw new PlaylistException("Generic error");
        }
    }
    
    //setTrackDetails method
	private void setTrackDetails(List<Track> tracksToAdd, int sizeIndex, Date lastUpdated, TrackPlayList playList,
			List<PlayListTrack> original, List<PlayListTrack> addedList) {
		for (Track track : tracksToAdd) {
		    PlayListTrack playlistTrack = new PlayListTrack();
		    playlistTrack.setTrack(track);
		    playlistTrack.setTrackPlaylist(playList);
		    int artistId = track.getArtistId(); //Extract local variable
			playlistTrack.setTrackArtistId(artistId);
		    playlistTrack.setDateAdded(lastUpdated);
		    playlistTrack.setTrack(track);
		    playList.setDuration(addTrackDurationToPlaylist(playList, track));
		    original.add(sizeIndex, playlistTrack);
		    addedList.add(playlistTrack);
		    sizeIndex++;
		}
		
	}
	
			//deleteTracks method
			public void deleteTracks(String uuid, int userId, List<PlayListTrack> tracksToDelete) 
					throws PlaylistException {
			
			try {
			
			TrackPlayList playList = playlistDaoBean.getPlaylistByUUID(uuid, userId);			
			Set<PlayListTrack> playListTracks = playList.getPlayListTracks();
			Set<PlayListTrack> originalSet = playListTracks;
			List<PlayListTrack> original;
			if (originalSet == null || originalSet.size() == 0)
			original = new ArrayList<PlayListTrack>();
			else
			original = new ArrayList<PlayListTrack>(originalSet);
			
			Collections.sort(original);
			
			List<PlayListTrack> deleteList = new ArrayList<PlayListTrack>(tracksToDelete.size());
			
			deleteTrackByID(tracksToDelete, playList, original, deleteList);
			
			int i = 0;
			for (PlayListTrack track : original) {
			track.setIndex(i++);
			}
			
			playListTracks.clear();
			playListTracks.addAll(original);
			playList.setNrOfTracks(original.size());
			

			
			} catch (Exception e) {
			e.printStackTrace();
			throw new PlaylistException("Generic error");
			}
			}
			
			// deleteTrackById method
			private void deleteTrackByID(List<PlayListTrack> tracksToDelete, TrackPlayList playList,
			List<PlayListTrack> original, List<PlayListTrack> deleteList) {
			for (PlayListTrack playListTrack : tracksToDelete) {
		
			int id = playListTrack.getId();
			playList.setDuration(subtractTrackDurationFromPlaylist(playList, playListTrack));
				for (PlayListTrack listTrack : original)
				{
					if (listTrack.getId() == id)
					{
						//delete track
						original.remove(listTrack);
						
					}
	
				}
			}
			}
	
	

    private boolean validateIndexes(int sizeIndex, int length) {
        return sizeIndex >= 0 && sizeIndex <= length;
    }

    private float addTrackDurationToPlaylist(TrackPlayList playList, Track track) {
        return (track != null ? track.getDuration() : 0)
                + (playList != null && playList.getDuration() != null ? playList.getDuration() : 0);
    }
    
    
    //added new function to subtract duration from the playlist when a track is deleted
    private float subtractTrackDurationFromPlaylist(TrackPlayList playList, PlayListTrack playListTrack) {
        return (playList != null && playList.getDuration() != null ? playList.getDuration() : 0)
        		- (playListTrack != null ? playListTrack.getTrack().getDuration() : 0);
    }
}
