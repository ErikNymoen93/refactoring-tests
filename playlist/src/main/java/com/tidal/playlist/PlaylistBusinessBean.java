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

    List<PlayListTrack> addTracks(String uuid, int userId, List<Track> tracksToAdd, int toIndex,
                                  Date lastUpdated) throws PlaylistException {

        try {

            TrackPlayList playList = playlistDaoBean.getPlaylistByUUID(uuid, userId);

            //We do not allow > 500 tracks in new playlists
            if (playList.getNrOfTracks() + tracksToAdd.size() > 500) {
                throw new PlaylistException("Playlist cannot have more than " + 500 + " tracks");
            }

            // The index is out of bounds, put it in the end of the list.
            if (toIndex > playList.getPlayListTracksSize() || toIndex == -1) {
                toIndex = playList.getPlayListTracksSize();
            }

            if (!validateIndexes(toIndex, playList.getNrOfTracks())) {
                return Collections.EMPTY_LIST;
            }

            Set<PlayListTrack> originalSet = playList.getPlayListTracks();
            List<PlayListTrack> original;
            if (originalSet == null || originalSet.size() == 0)
                original = new ArrayList<PlayListTrack>();
            else
                original = new ArrayList<PlayListTrack>(originalSet);

            Collections.sort(original);

            List<PlayListTrack> added = new ArrayList<PlayListTrack>(tracksToAdd.size());

            for (Track track : tracksToAdd) {
                PlayListTrack playlistTrack = new PlayListTrack();
                playlistTrack.setTrack(track);
                playlistTrack.setTrackPlaylist(playList);
                playlistTrack.setTrackArtistId(track.getArtistId());
                playlistTrack.setDateAdded(lastUpdated);
                playlistTrack.setTrack(track);
                playList.setDuration(addTrackDurationToPlaylist(playList, track));
                original.add(toIndex, playlistTrack);
                added.add(playlistTrack);
                toIndex++;
            }

            int i = 0;
            for (PlayListTrack track : original) {
                track.setIndex(i++);
            }

            playList.getPlayListTracks().clear();
            playList.getPlayListTracks().addAll(original);
            playList.setNrOfTracks(original.size());

            return added;

        } catch (Exception e) {
            e.printStackTrace();
            throw new PlaylistException("Generic error");
        }
    }

    private boolean validateIndexes(int toIndex, int length) {
        return toIndex >= 0 && toIndex <= length;
    }

    private float addTrackDurationToPlaylist(TrackPlayList playList, Track track) {
        return (track != null ? track.getDuration() : 0)
                + (playList != null && playList.getDuration() != null ? playList.getDuration() : 0);
    }
}
