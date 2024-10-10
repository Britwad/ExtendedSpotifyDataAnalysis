package SpotifyDataAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class SpotifyAnalysis implements Serializable {

    private List<SpotifyStream> streamingHistory;
    private Set<SpotifyStream> allStreams;
    private Set<String> allAlbums;
    private Set<String> allArtists;
    private Set<String> allPodcasts;
    private int totalStreams;

    private Set<LibraryStream> likedSongs;

    private final SpotifyStreamComparator spotifyStreamComparator;

    //STREAM FILTERS (They will affect what is collected directly from endsong_ files and what is counted for total stream time)
    private int minimumListenTime; //Default: 0
    private int maximumListenTime; //Default: -1 (Fun to see most skipped tracks)
    private Date minimumDate; //Default: null
    private Date maximumDate; //Default: null
    private boolean noSkips; //Default: false
    private boolean musicOnly; //Default: false
    private boolean podcastsOnly; //Default: false (MUSIC_ONLY AND PODCASTS_ONLY CANNOT BOTH BE TRUE, WILL DEFAULT TO BOTH)
    private boolean onlineOnly; //Default: false
    private boolean offlineOnly; //Default: false (ONLINE_ONLY AND OFFLINE_ONLY CANNOT BOTH BE TRUE, WILL DEFAULT TO BOTH)
    private int divider; //Default: 60000 (Minutes)

    public SpotifyAnalysis(File[] historyDirect) {
        minimumListenTime = 0;
        maximumListenTime = -1;
        minimumDate = null;
        maximumDate = null;
        noSkips = false;
        musicOnly = false;
        podcastsOnly = false;
        onlineOnly = false;
        offlineOnly = false;
        divider = 60000;

        spotifyStreamComparator  = new SpotifyStreamComparator(SpotifyStreamComparator.COMPARE_TRACK_ARTIST);

        loadStreamingHistory(historyDirect);
    }
    public SpotifyAnalysis(File[] historyDirect, File yourLibrary) {
        this(historyDirect);
        this.likedSongs = getLikedSongs(yourLibrary);
    }

    public void printStreamingInfo() {
        System.out.println("Total Streams: " + streamingHistory.size() + " / " + totalStreams);
        System.out.println();

        System.out.println("Total Tracks: " + allStreams.size());
        System.out.println("Total Albums: " + allAlbums.size());
        System.out.println("Total Artists: " + allArtists.size());
        System.out.println();

        long milliseconds = getTotalStreamTimeMs();
        System.out.println("Total Days: " + milliseconds/(86400000));
        System.out.println("Total Hours: " + milliseconds/3600000);
        System.out.println("Total Minutes: " + milliseconds/60000);
        System.out.println("Total Seconds: " + milliseconds/1000);
        System.out.println("Total Ms: " + milliseconds);

        if (streamingHistory.size()>0) {
            System.out.println();
            System.out.println("Earliest Date: " + getEarliestDate());
            System.out.println("Latest Date: " + getLatestDate());

            if (!podcastsOnly) {
                System.out.println();
                System.out.println("Average Song Length: " + averageFullSongLength());
            }
        }
    }

    //DATA ANALYSIS-----------------------------------------------------------------------------------------------------
    /*
    Following methods retrieve a list of "generic streams" where streams "uniqueness" is simply judged based on
    spotify Uri. Streams can be used to get information on artist or album name given a string or
    spotify Uri. HOWEVER information in the stream that varies between streams of the same song SHOULD NOT BE USED generally
     */
    //STREAM TRACK ANALYSIS
    public Map<SpotifyStream, Integer> getStreamFrequency() {
        return getStreamFrequency(spotifyStreamComparator);
    }
    public Map<SpotifyStream, Integer> getAlbumStreamFrequency(String album) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory)
            if (s.album.equalsIgnoreCase(album)) streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        return streams;
    }
    public Map<SpotifyStream, Integer> getStreamFrequency(String artist) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory)
            if (s.artist.equalsIgnoreCase(artist)) streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        return streams;
    }
    public Map<SpotifyStream, Integer> getStreamFrequency(Date d1, Date d2) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        }
        return streams;
    }
    public Map<SpotifyStream, Integer> getStreamFrequency(Date d1, Date d2, String artist) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.artist.equalsIgnoreCase(artist))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        }
        return streams;
    }
    public Map<SpotifyStream, Integer> getAlbumStreamFrequency(Date d1, Date d2, String album) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.album.equalsIgnoreCase(album))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        }
        return streams;
    }

    public Map<SpotifyStream, Integer> getStreamPlaytime() {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        }
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    public Map<SpotifyStream, Integer> getAlbumStreamPlaytime(String album) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (s.album.equalsIgnoreCase(album)) streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        }
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    public Map<SpotifyStream, Integer> getStreamPlaytime(String artist) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (s.artist.equalsIgnoreCase(artist)) streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        }
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    public Map<SpotifyStream, Integer> getStreamPlaytime(Date d1, Date d2) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        }
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    public Map<SpotifyStream, Integer> getStreamPlaytime(Date d1, Date d2, String artist) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.artist.equalsIgnoreCase(artist))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        }
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    public Map<SpotifyStream, Integer> getAlbumStreamPlaytime(Date d1, Date d2, String album) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.album.equalsIgnoreCase(album))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        }
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    //STREAM EPISODE ANALYSIS
    public Map<SpotifyStream, Integer> getPodcastStreamFrequency(String podcast) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory)
            if (s.episode_show_name.equalsIgnoreCase(podcast)) streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        return streams;
    }
    public Map<SpotifyStream, Integer> getPodcastStreamFrequency(Date d1, Date d2, String podcast) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory)
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.episode_show_name.equalsIgnoreCase(podcast))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        return streams;
    }

    public Map<SpotifyStream, Integer> getPodcastStreamPlaytime(String podcast) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory)
            if (s.episode_show_name.equalsIgnoreCase(podcast))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    public Map<SpotifyStream, Integer> getPodcastStreamPlaytime(Date d1, Date d2, String podcast) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streamingHistory)
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.episode_show_name.equalsIgnoreCase(podcast))
                streams.put(s, streams.containsKey(s) ? streams.get(s)+s.msPlayed : s.msPlayed);
        streams.replaceAll((s, v) -> v / divider);
        return streams;
    }
    //ALBUM ANALYSIS
    public Map<String, Integer> getAlbumFrequency() {
        HashMap<String,Integer> albumFrequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            albumFrequency.put(s.album, albumFrequency.containsKey(s.album) ?
                    albumFrequency.get(s.album)+1 : 1);
        }
        return albumFrequency;
    }
    public Map<String, Integer> getAlbumFrequency(String artist) {
        HashMap<String,Integer> albumFrequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (s.artist.equalsIgnoreCase(artist))
                albumFrequency.put(s.album, albumFrequency.containsKey(s.album) ?
                        albumFrequency.get(s.album)+1 : 1);
        }
        return albumFrequency;
    }
    public Map<String, Integer> getAlbumFrequency(Date d1, Date d2) {
        HashMap<String,Integer> albumFrequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                albumFrequency.put(s.album, albumFrequency.containsKey(s.album) ?
                    albumFrequency.get(s.album)+1 : 1);
        }
        return albumFrequency;
    }
    public Map<String, Integer> getAlbumFrequency(Date d1, Date d2, String artist) {
        HashMap<String,Integer> albumFrequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.artist.equalsIgnoreCase(artist))
                albumFrequency.put(s.album, albumFrequency.containsKey(s.album) ?
                        albumFrequency.get(s.album)+1 : 1);
        }
        return albumFrequency;
    }

    public Map<String, Integer> getAlbumPlaytime() {
        HashMap<String,Integer> playTime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            playTime.put(s.album, playTime.containsKey(s.album) ?
                    playTime.get(s.album)+s.msPlayed : s.msPlayed);
        }
        playTime.replaceAll((s, v) -> v / divider);
        return playTime;
    }
    public Map<String, Integer> getAlbumPlaytime(String artist) {
        HashMap<String,Integer> playTime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (s.artist.equalsIgnoreCase(artist))
                playTime.put(s.album, playTime.containsKey(s.album) ?
                        playTime.get(s.album)+s.msPlayed : s.msPlayed);
        }
        playTime.replaceAll((s, v) -> v / divider);
        return playTime;
    }
    public Map<String, Integer> getAlbumPlaytime(Date d1, Date d2) {
        HashMap<String,Integer> playTime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                playTime.put(s.album, playTime.containsKey(s.album) ?
                    playTime.get(s.album)+s.msPlayed : s.msPlayed);
        }
        playTime.replaceAll((s, v) -> v / divider);
        return playTime;
    }
    public Map<String, Integer> getAlbumPlaytime(Date d1, Date d2, String artist) {
        HashMap<String,Integer> playTime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0) & s.artist.equalsIgnoreCase(artist))
                playTime.put(s.album, playTime.containsKey(s.album) ?
                        playTime.get(s.album)+s.msPlayed : s.msPlayed);
        }
        playTime.replaceAll((s, v) -> v / divider);
        return playTime;
    }

    //ARTIST ANALYSIS
    public Map<String, Integer> getArtistFrequency() {
        HashMap<String,Integer> artistFrequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            artistFrequency.put(s.artist, artistFrequency.containsKey(s.artist) ?
                    artistFrequency.get(s.artist)+1 : 1);
        }
        return artistFrequency;
    }
    public Map<String, Integer> getArtistFrequency(Date d1, Date d2) {
        HashMap<String,Integer> artistFrequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                artistFrequency.put(s.artist, artistFrequency.containsKey(s.artist) ?
                    artistFrequency.get(s.artist)+1 : 1);
        }
        return artistFrequency;
    }

    public Map<String, Integer> getArtistPlaytime() {
        HashMap<String,Integer> playTime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            playTime.put(s.artist, playTime.containsKey(s.artist) ?
                    playTime.get(s.artist)+s.msPlayed : s.msPlayed);
        }
        playTime.replaceAll((s, v) -> v / divider);
        return playTime;
    }
    public Map<String, Integer> getArtistPlaytime(Date d1, Date d2) {
        HashMap<String,Integer> playTime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                playTime.put(s.artist, playTime.containsKey(s.artist) ?
                    playTime.get(s.artist)+s.msPlayed : s.msPlayed);
        }
        playTime.replaceAll((s, v) -> v / divider);
        return playTime;
    }

    //PODCAST ANALYSIS
    public Map<String, Integer> getPodcastFrequency() {
        HashMap<String,Integer> frequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            frequency.put(s.episode_show_name, frequency.containsKey(s.episode_show_name) ?
                    frequency.get(s.episode_show_name)+1 : 1);
        }
        return frequency;
    }
    public Map<String, Integer> getPodcastFrequency(Date d1, Date d2) {
        HashMap<String,Integer> frequency = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                frequency.put(s.episode_show_name, frequency.containsKey(s.episode_show_name) ?
                    frequency.get(s.episode_show_name)+1 : 1);
        }
        return frequency;
    }

    public Map<String, Integer> getPodcastPlaytime() {
        HashMap<String,Integer> playtime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            playtime.put(s.episode_show_name, playtime.containsKey(s.episode_show_name) ?
                    playtime.get(s.episode_show_name)+s.msPlayed : s.msPlayed);
        }
        playtime.replaceAll((s, v) -> v / divider);
        return playtime;
    }
    public Map<String, Integer> getPodcastPlaytime(Date d1, Date d2) {
        HashMap<String,Integer> playtime = new HashMap<>();
        for (SpotifyStream s: streamingHistory) {
            if (d1==null||(s.date.compareTo(d1)>=0)&(d2==null||s.date.compareTo(d2)<0))
                playtime.put(s.episode_show_name, playtime.containsKey(s.episode_show_name) ?
                    playtime.get(s.episode_show_name)+s.msPlayed : s.msPlayed);
        }
        playtime.replaceAll((s, v) -> v / divider);
        return playtime;
    }

    //FIRST OCCURRENCE
    public SpotifyStream getFirstTrackOccurrence(String track, String artist, Date d1) {
        SpotifyStream min = null;
        for (SpotifyStream s: streamingHistory) {
            if (s.track.equalsIgnoreCase(track)&s.artist.equalsIgnoreCase(artist) & (d1==null||s.date.compareTo(d1)>=0)) {
                if (min==null || s.date.compareTo(min.date)<0) min = s;
            }
        }
        return min;
    }
    public SpotifyStream getFirstAlbumOccurrence(String album, String artist, Date d1) {
        SpotifyStream min = null;
        for (SpotifyStream s: streamingHistory) {
            if (s.album.equalsIgnoreCase(album)&s.artist.equalsIgnoreCase(artist) & (d1==null||s.date.compareTo(d1)>=0)) {
                if (min==null || s.date.compareTo(min.date)<0) min = s;
            }
        }
        return min;
    }
    public SpotifyStream getFirstArtistOccurrence(String artist, Date d1) {
        SpotifyStream min = null;
        for (SpotifyStream s: streamingHistory) {
            if (s.artist.equalsIgnoreCase(artist) & (d1==null||s.date.compareTo(d1)>=0)) {
                if (min==null || s.date.compareTo(min.date)<0) min = s;
            }
        }
        return min;
    }
    //OTHER ANALYSIS
    private long getTotalStreamTimeMs() {
        long ms = 0;
        for (SpotifyStream s: streamingHistory) {
            ms+=s.msPlayed;
        }
        return ms;
    }
    private Date getEarliestDate() {
        Iterator<SpotifyStream> i = streamingHistory.iterator();
        Date minDate = i.next().date;
        while (i.hasNext()) {
            Date next = i.next().date;
            if (next.compareTo(minDate)<0) minDate = next;
        }
        return minDate;
    }
    private Date getLatestDate() {
        Iterator<SpotifyStream> i = streamingHistory.iterator();
        Date maxDate = i.next().date;
        while (i.hasNext()) {
            Date next = i.next().date;
            if (next.compareTo(maxDate)>0) maxDate = next;
        }
        return maxDate;
    }
    private String averageFullSongLength() {
        long ms = 0;
        int songCount = 0;
        for (SpotifyStream s: streamingHistory) {
            if (!s.track.equals("null") & s.reason_end.equals("trackdone")) {
                ms += s.msPlayed;
                songCount++;
            }
        }
        double average = (double)(ms/songCount)/60000;
        return (int)average + ":" + minTwoDigits((int)((average-(int)average)*60));
    }

    //OTHER ACCESSORS
    public boolean trackExists(String track, String artist) {
        for (SpotifyStream s: allStreams) {
            if (s.track.equalsIgnoreCase(track)&s.artist.equalsIgnoreCase(artist)) return true;
        }
        return false;
    }
    public boolean artistExists(String artist) {
        return allArtists.contains(artist.toLowerCase());
    }
    public boolean albumExists(String album) {
        return allAlbums.contains(album.toLowerCase());
    }
    public boolean podcastExists(String podcast) {
        return allPodcasts.contains(podcast.toLowerCase());
    }

    public boolean albumByArtist(String album, String artist) {
        for (SpotifyStream s: allStreams) {
            if (s.album.equalsIgnoreCase(album)&s.artist.equalsIgnoreCase(artist)) return true;
        }
        return false;
    }
    
    //LIKED SONG ACCESSORS
    public boolean isALikedSong(SpotifyStream s) {
        for (LibraryStream ls: likedSongs)
            if (spotifyStreamComparator.compareToLibraryStream(s,ls)==0) return true;
        return false;
    }
    public boolean likedSongsHaveArtist(String artist) {
        return artistCount(artist)!=0;
    }
    public int likedSongCount() {
        return likedSongs.size();
    }
    public int artistCount(String artist) {
        int artistCount = 0;
        for (LibraryStream ls: likedSongs) {
            if (ls.artist.equalsIgnoreCase(artist)) artistCount++;
        }
        return artistCount;
    }

    //DATA MANIPULATION-------------------------------------------------------------------------------------------------
    private Map<SpotifyStream,Integer> refineStreamCollection(Map<SpotifyStream,Integer> streams) {
        //Slow and needs improvement, switches every stream with one containing the most reoccurring uri link
        Map<SpotifyStream, Integer> uriFrequency = getStreamFrequency(new SpotifyStreamComparator()); //Uses Default Uri comparison
        Map<SpotifyStream, Integer> refinedMap = new TreeMap<>(spotifyStreamComparator);
        for (SpotifyStream s: streams.keySet()) {
            Iterator<SpotifyStream> i = uriFrequency.keySet().iterator();
            SpotifyStream maxStream = null;
            int max = 0;
            while (i.hasNext()) {
                SpotifyStream uri = i.next();
                if (spotifyStreamComparator.compare(uri,s)==0 & uriFrequency.get(s)>max) {
                    max = uriFrequency.get(s);
                    maxStream = s;
                }
            }
            refinedMap.put(maxStream,streams.get(s));
        }
        return refinedMap;
    }
    private Map<SpotifyStream, Integer> getStreamFrequency(Comparator<SpotifyStream> comparator) {
        Map<SpotifyStream, Integer> streams = new TreeMap<>(comparator);
        for (SpotifyStream s: streamingHistory) streams.put(s, streams.containsKey(s) ? streams.get(s)+1 : 1);
        return streams;
    } //Helper^^

    private static void filterOutEpisodes(Map<SpotifyStream, Integer> streams) {
        streams.keySet().removeIf(spotifyStream -> spotifyStream.track.equals("null"));
    }
    private static void filterOutTracks(Map<SpotifyStream, Integer> streams) {
        streams.keySet().removeIf(spotifyStream -> spotifyStream.episode_name.equals("null"));
    }
    private static void filterOutNull(Map<String, Integer> streams) {
        streams.keySet().removeIf(s -> s.equals("null"));
    }
    //STREAM DATA COLLECTION--------------------------------------------------------------------------------------------
    public void loadStreamingHistory(File[] historyDirect) {
        totalStreams = 0;
        streamingHistory = new LinkedList<>();
        for (File f: historyDirect) {
            getStreams(f);
        }

        allStreams = new TreeSet<>(spotifyStreamComparator);
        allAlbums = new HashSet<>();
        allArtists = new HashSet<>();
        allPodcasts = new HashSet<>();

        for (SpotifyStream s: streamingHistory) {
            allStreams.add(s);
            allAlbums.add(s.album.toLowerCase());
            allArtists.add(s.artist.toLowerCase());
            allPodcasts.add(s.episode_show_name.toLowerCase());
        }
    }
    private void getStreams(File f) {
        try {
            String[] history = new Scanner(f).nextLine().split("\\{");
            List<SpotifyStream> streams = new ArrayList<>();
            Date date;
            String username;
            String platform;
            int msPlayed;
            String conn_country;
            String ip_addr_decrypted;
            String user_agent_decrypted;
            String track;
            String artist;
            String album;
            String spotify_track_uri;
            String episode_name;
            String episode_show_name;
            String spotify_episode_uri;
            String reason_start;
            String reason_end;
            Boolean shuffle; // null/true/false
            Boolean skipped; // null/true/false
            Boolean offline; // null/true/false
            String offline_timestamp;
            Boolean incognito_mode; // null/true/false
            for (int i = 1;i<history.length;i++) {
                String streamData = history[i];
                date = timestampToDate(extractString(streamData,"ts"));
                username = extractString(streamData,"username");
                platform = extractString(streamData,"platform");
                msPlayed = Integer.parseInt(extractString(streamData,"ms_played"));
                conn_country = extractString(streamData,"conn_country");
                ip_addr_decrypted = extractString(streamData,"ip_addr_decrypted");
                user_agent_decrypted = extractString(streamData, "user_agent_decrypted");
                track = extractString(streamData, "master_metadata_track_name");
                artist = extractString(streamData, "master_metadata_album_artist_name");
                album = extractString(streamData, "master_metadata_album_album_name");
                spotify_track_uri = extractString(streamData, "spotify_track_uri");
                episode_name = extractString(streamData, "episode_name");
                episode_show_name = extractString(streamData, "episode_show_name");
                spotify_episode_uri = extractString(streamData, "spotify_episode_uri");
                reason_start = extractString(streamData, "reason_start");
                reason_end = extractString(streamData, "reason_end");
                shuffle = toBoolean(extractString(streamData, "shuffle"));
                skipped = toBoolean(extractString(streamData, "skipped"));
                offline = toBoolean(extractString(streamData, "offline"));
                offline_timestamp = extractString(streamData, "offline_timestamp");
                incognito_mode = toBoolean(extractString(streamData, "incognito_mode"));
                if (    (msPlayed>= minimumListenTime) &
                        (maximumListenTime < 0 | msPlayed< maximumListenTime) &
                        (!noSkips || reason_end.equals("trackdone")) &
                        (minimumDate ==null|date.compareTo(minimumDate)>=0) &
                        (maximumDate ==null|date.compareTo(maximumDate)<0) &
                        ((musicOnly & podcastsOnly | (!musicOnly | !track.equals("null")))) &
                        ((musicOnly & podcastsOnly | (!podcastsOnly | !episode_name.equals("null")))) &
                        ((offlineOnly & onlineOnly) | (!offlineOnly | Boolean.TRUE.equals(offline))) &
                        ((offlineOnly & onlineOnly) | (!onlineOnly | Boolean.FALSE.equals(offline)))) {
                    streams.add(new SpotifyStream(date, username, platform, msPlayed, conn_country, ip_addr_decrypted,
                            user_agent_decrypted, track, artist, album, spotify_track_uri, episode_name,
                            episode_show_name, spotify_episode_uri, reason_start, reason_end, shuffle, skipped, offline,
                            offline_timestamp, incognito_mode));
                }
                totalStreams++;
            }
            streamingHistory.addAll(streams);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private Set<LibraryStream> getLikedSongs(File f) {
        Set<LibraryStream> likedSongs = new HashSet<>();
        try {
            Scanner scanner = new Scanner(f);
            while (scanner.hasNext()) {
                if (scanner.nextLine().equals("  \"tracks\": [")) break;
            }
            while (!scanner.nextLine().equals("  ],")) {
                likedSongs.add(new LibraryStream(
                        correctLikedSongString(extractString(scanner.nextLine(),"artist")),
                        correctLikedSongString(extractString(scanner.nextLine(),"album")),
                        correctLikedSongString(extractString(scanner.nextLine(),"track")),
                        correctLikedSongString(extractString(scanner.nextLine() + ",","uri"))
                        ));
                scanner.nextLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return likedSongs;
    }
    private static String correctLikedSongString(String s) { //ONLY FOR GET LIKED SONGS
        return s.substring(2,s.length()-1);
    }

    //STATIC HELPERS----------------------------------------------------------------------------------------------------
    private static String extractString(String streamData, String key) {
        if (streamData.contains("]")) streamData = streamData.substring(0,streamData.length()-1) + ",";
        key = "\"" + key + "\":";
        int stringIndex = streamData.indexOf(key)+key.length();
        if (streamData.charAt(stringIndex)=='"') return streamData.substring(stringIndex+1,streamData.indexOf("\"",stringIndex+2));
        return streamData.substring(stringIndex,streamData.indexOf(",",stringIndex+1));
    }
    private static Boolean toBoolean(String s) {
        if (s.endsWith("}")) s = s.substring(0,s.length()-1);
        if (s.equals("true")) return true;
        else if (s.equals("false")) return false;
        else return null;
    }
    private static Date timestampToDate(String timestamp) {
        String[] ts = timestamp.split("T");
        return new Date(
                Integer.parseInt(ts[0].split("-")[0]),
                Integer.parseInt(ts[0].split("-")[1]),
                Integer.parseInt(ts[0].split("-")[2]),

                Integer.parseInt(ts[1].split(":")[0]),
                Integer.parseInt(ts[1].split(":")[1]),
                Integer.parseInt(ts[1].split(":")[2].split("Z")[0])
        );
    }
    private static String minTwoDigits(int n) {
        if (n>=0&n<10) return "0" + n;
        else return Integer.toString(n);
    }
    private static HashMap<String, Integer> sortByValue(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> valueSorted = new LinkedList<>(map.entrySet());
        valueSorted.sort(Map.Entry.comparingByValue());
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : valueSorted) temp.put(entry.getKey(), entry.getValue());
        return temp;
    }


    //OUTPUT DATA-------------------------------------------------------------------------------------------------------
    private static HashMap<SpotifyStream, Integer> sortStreamByValueAscending(Map<SpotifyStream, Integer> map) {
        List<Map.Entry<SpotifyStream, Integer>> valueSorted = new LinkedList<>(map.entrySet());
        valueSorted.sort(Map.Entry.comparingByValue());
        HashMap<SpotifyStream, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<SpotifyStream, Integer> entry : valueSorted) temp.put(entry.getKey(), entry.getValue());
        return temp;
    }
    private static HashMap<SpotifyStream, Integer> sortStreamByValueDescending(Map<SpotifyStream, Integer> map) {
        List<Map.Entry<SpotifyStream, Integer>> valueSorted = new LinkedList<>(map.entrySet());
        valueSorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        HashMap<SpotifyStream, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<SpotifyStream, Integer> entry : valueSorted) temp.put(entry.getKey(), entry.getValue());
        return temp;
    }

    public static ArrayList<String> getSortedTop(Map<String,Integer> map, int n) {
        filterOutNull(map);
        ArrayList<String> output = new ArrayList<>();
        Map<String,Integer> sortedMap = sortByValue(map);
        int i = 0;
        int size = sortedMap.keySet().size();
        for (String artist: sortedMap.keySet()) {
            if (size-i<=n) {
                output.add(artist);
            }
            i++;
        }
        return output;
    }
    public static String sortTop(Map<String,Integer> map, int n) {
        filterOutNull(map);
        StringBuilder output = new StringBuilder();
        Map<String,Integer> sortedMap = sortByValue(map);
        int i = 0;
        int size = sortedMap.keySet().size();
        for (String artist: sortedMap.keySet()) {
            if (size-i<=n) {
                output.append(size - i).append(": ").append(artist).append(": ").append(sortedMap.get(artist)).append("\n");
            }
            i++;
        }
        return output.toString();
    }
    public static String sortTop(Map<String,Integer> map) {
        return sortTop(map,map.size());
    }
    public static String sortTopTracks(Map<SpotifyStream,Integer> streams, int n) {
        filterOutEpisodes(streams);
        StringBuilder output = new StringBuilder();
        SpotifyStream[] set = sortStreamByValueAscending(streams).keySet().toArray(SpotifyStream[]::new);
        if (n>set.length)n=set.length;
        for (int i = set.length-n;i<set.length;i++) {
            output.append(set.length - i).append(": ").append(set[i].track).append(": ").append(streams.get(set[i])).append("\n");
        }
        return output.toString();
    }
    public static String sortTopTracks(Map<SpotifyStream,Integer> streams) {
        return sortTopTracks(streams,streams.size());
    }
    public static String sortTopEpisodes(Map<SpotifyStream,Integer> streams, int n) {
        filterOutTracks(streams);
        StringBuilder output = new StringBuilder();
        SpotifyStream[] set = sortStreamByValueAscending(streams).keySet().toArray(SpotifyStream[]::new);
        if (n>set.length)n=set.length;
        for (int i = set.length-n;i<set.length;i++) {
            output.append(set.length - i).append(": ").append(set[i].episode_name).append(": ").append(streams.get(set[i])).append("\n");
        }
        return output.toString();
    }
    public static String sortTopUris(Map<SpotifyStream,Integer> streams, int n) {
        StringBuilder output = new StringBuilder();
        SpotifyStream[] set = sortStreamByValueAscending(streams).keySet().toArray(SpotifyStream[]::new);
        for (int i = set.length-1;i>=set.length-n&i>=0;i--) output.append(set[i].spotify_track_uri).append("\n");
        return output.toString();
    }

    public static LinkedList<SpotifyStream> sortPlaylist(Map<SpotifyStream,Integer> streams,int n) {
        return sortPlaylist(streams,n,null);
    }
    public static LinkedList<SpotifyStream> sortPlaylist(Map<SpotifyStream,Integer> streams, int n, ArrayList<String> artistFilter) {
        if (artistFilter!=null) {
//            ArrayList<SpotifyStream> filteredOut = new ArrayList<>();
//            for (SpotifyStream s : streams.keySet()) {
//                if (artistFilter.contains(s.artist)) filteredOut.add(s);
//            }
//            for (SpotifyStream s: filteredOut) streams.remove(s);
            streams.keySet().removeIf(spotifyStream -> artistFilter.contains(spotifyStream.artist));
        }
        filterOutEpisodes(streams);
        LinkedList<SpotifyStream> playlist = new LinkedList<>(sortStreamByValueDescending(streams).keySet());
        n = Math.min(playlist.size(),n);
        while (playlist.size()>=n) playlist.removeLast();
        return playlist;
    }
    //ACCESSORS----------------------------------------------------------------------------------------------------------
    public int getMinimumListenTime() {return minimumListenTime;}
    public int getMaximumListenTime() {return maximumListenTime;}
    public Date getMinimumDate() {return minimumDate;}
    public Date getMaximumDate() {return maximumDate;}
    public boolean getNoSkips() {return noSkips;}
    public boolean getMusicOnly() {
        return musicOnly;
    }
    public boolean getPodcastsOnly() {
        return podcastsOnly;
    }
    public boolean getOnlineOnly() {
        return onlineOnly;
    }
    public boolean getOfflineOnly() {
        return offlineOnly;
    }
    public int getSpotifyStreamComparatorSetting() {
        return spotifyStreamComparator.getComparisonSetting();
    }
    public int getDivider() {
        return this.divider;
    }

    public int getArtistCount() {
        return allArtists.size();
    }
    public int getPodcastCount() {
        return allPodcasts.size();
    }
    public boolean hasLikedSongs() {
        return likedSongs!=null;
    }
    //MUTATORS----------------------------------------------------------------------------------------------------------
    public void setMinimumListenTime(int n) {minimumListenTime=n;}
    public void setMaximumListenTime(int n) {maximumListenTime=n;}
    public void setMinimumDate(Date d) {minimumDate=d;}
    public void setMaximumDate(Date d) {maximumDate=d;}
    public void setNoSkips(boolean b) {noSkips=b;}
    public void setMusicOnly(boolean b) {
        musicOnly=b;
        if (musicOnly) podcastsOnly=false;
    }
    public void setPodcastsOnly(boolean b) {
        podcastsOnly=b;
        if (podcastsOnly) musicOnly=false;
    }
    public void setOnlineOnly(boolean b) {
        onlineOnly=b;
        if (onlineOnly) offlineOnly=false;
    }
    public void setOfflineOnly(boolean b) {
        offlineOnly=b;
        if (offlineOnly) onlineOnly=false;
    }
    public void setSpotifyStreamComparator(int i) {
        spotifyStreamComparator.setComparisonSetting(i);
    }
    public void setDivider(int i) {this.divider=i;}

    //NESTED ClASSES----------------------------------------------------------------------------------------------------
    public static class SpotifyStreamComparator implements Comparator<SpotifyStream>, Serializable {

        //In order of strictness descending
        public static final int COMPARE_URI = 0;
        public static final int COMPARE_TRACK_ALBUM_ARTIST = 3;
        public static final int COMPARE_TRACK_ARTIST = 2;
        public static final int COMPARE_TRACK = 1;

        private int comparisonSetting;

        public SpotifyStreamComparator() {
            this(0);
        }
        public SpotifyStreamComparator(int setting) {
            setComparisonSetting(setting);
        }

        public int compare(SpotifyStream o1, SpotifyStream o2) {
            return toComparableString(o1).compareTo(toComparableString(o2));
        }
        public int compareToLibraryStream(SpotifyStream o1, LibraryStream o2) {
            return toComparableString(o1).compareTo(toComparableString(o2));
        }
        //Creates the String that will be used to compare SpotifyStream objects based on assigned setting
        private String toComparableString(SpotifyStream s) {
            if (comparisonSetting ==COMPARE_URI) {
                if (s.spotify_episode_uri.equals("null")) return s.spotify_track_uri;
                else return s.spotify_episode_uri;
            }
            else if (comparisonSetting ==COMPARE_TRACK) return s.track.equals("null")?s.episode_name:s.track;
            else if (comparisonSetting ==COMPARE_TRACK_ARTIST) return s.track.equals("null")?s.episode_name+s.episode_show_name:s.track + s.artist;
            else if (comparisonSetting ==COMPARE_TRACK_ALBUM_ARTIST) return s.track.equals("null")?s.episode_name+s.episode_show_name: s.track + s.album + s.artist;
            else return "";
        }
        private String toComparableString(LibraryStream s) {
            if (comparisonSetting == COMPARE_URI) return s.uri;
            else if (comparisonSetting == COMPARE_TRACK) return s.track;
            else if (comparisonSetting == COMPARE_TRACK_ARTIST) return s.track + s.artist;
            else if (comparisonSetting == COMPARE_TRACK_ALBUM_ARTIST) return s.track + s.album + s.artist;
            else return "";
        }

        public void setComparisonSetting(int setting) {
            if (setting>=0&setting<4) this.comparisonSetting = setting;
            else this.comparisonSetting = COMPARE_URI;
        }
        public int getComparisonSetting() {
            return comparisonSetting;
        }
    }
    private static class LibraryStream {
        public final String artist;
        public final String album;
        public final String track;
        public final String uri;

        public LibraryStream(String artist, String album, String track, String uri) {
            this.artist = artist;
            this.album = album;
            this.track = track;
            this.uri = uri;
        }
        public String toString() {
            return "[" + artist + ", " + album + ", " + track + ", " + uri + "]";
        }
    }
}
