package SpotifyDataAnalysis;

import java.io.Serializable;
import java.util.Comparator;

public class SpotifyStream implements Serializable{

    public final Date date;
    public final String username;
    public final String platform;
    public final int msPlayed;
    public final String conn_country;
    public final String ip_addr_decrypted;
    public final String user_agent_decrypted;
    public final String track;
    public final String artist;
    public final String album;
    public final String spotify_track_uri;
    public final String episode_name;
    public final String episode_show_name;
    public final String spotify_episode_uri;
    public final String reason_start;
    public final String reason_end;

    public final Boolean shuffle;//": null/true/false,
    public final Boolean skipped;//": null/true/false,
    public final Boolean offline;//": null/true/false,
    public final String offline_timestamp;
    public final Boolean incognito_mode;//": null/true/false,

    public SpotifyStream(Date date, String username, String platform, int msPlayed, String conn_country,
                         String ip_addr_decrypted, String user_agent_decrypted, String track, String artist,
                         String album, String spotify_track_uri, String episode_name, String episode_show_name,
                         String spotify_episode_uri, String reason_start, String reason_end, Boolean shuffle,
                         Boolean skipped, Boolean offline, String offline_timestamp, Boolean incognito_mode) {
        this.date = date;
        this.username = username;
        this.platform = platform;
        this.msPlayed = msPlayed;
        this.conn_country = conn_country;
        this.ip_addr_decrypted = ip_addr_decrypted;
        this.user_agent_decrypted = user_agent_decrypted;
        this.track = track;
        this.artist = artist;
        this.album = album;
        this.spotify_track_uri = spotify_track_uri;
        this.episode_name = episode_name;
        this.episode_show_name = episode_show_name;
        this.spotify_episode_uri = spotify_episode_uri;
        this.reason_start = reason_start;
        this.reason_end = reason_end;
        this.shuffle = shuffle;
        this.skipped = skipped;
        this.offline = offline;
        this.offline_timestamp = offline_timestamp;
        this.incognito_mode = incognito_mode;
    }

    public String toString() {
        return  "[ts: " + date + ", " +
                "username: " + username + ", " +
                "platform: " + platform + ", " +
                "msPlayed: " + msPlayed + ", " +
                "conn_Country: " + conn_country + ", " +
                "Ip_addr_decrypted: " + ip_addr_decrypted + ", " +
                "user_agent_decrypted " + user_agent_decrypted + ", " +
                "track: " + track + ", " +
                "artist: " + artist + ", " +
                "album: " + album + ", " +
                "spotify_track_uri: " + spotify_track_uri + ", " +
                "episode_name: " + episode_name + ", " +
                "episode_show_name: " + episode_show_name + ", " +
                "spotify_episode_url: " + spotify_episode_uri + ", " +
                "reason_start: " + reason_start + ", " +
                "reason_end: " + reason_end + ", " +
                "shuffle: " + shuffle + ", " +
                "skipped: " + skipped + ", " +
                "offline: " + offline + ", " +
                "offline_timestamp: " + offline_timestamp + ", " +
                "incognito_mode: " + incognito_mode + "]";
    }


}
