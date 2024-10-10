package SpotifyDataAnalysis;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.*;
import java.util.List;

public class ClientMain {

    private static SpotifyAnalysis spotifyAnalysis;
    private static Scanner scanner;

    private static final int titleLength = 80;

    public static void main(String[] args) {
        System.out.println("Welcome to Brit's SpotifyDataAnalysis 2.0 Beta");
        System.out.println("Loading...");
        retrieveSpotifyAnalysis();
        scanner = new Scanner(System.in);
        while (true) {
            printTitle("Please select an option:");
            String[] options = spotifyAnalysis.likedSongCount()==0?
                    new String[]{"View Streaming Statistics", "View Streaming Charts", "Create a Playlist", "View Created Playlists", "Help", "Settings", "Save and Exit"}:
                    new String[]{"View Streaming Statistics", "View Streaming Charts", "Create a Playlist", "View Created Playlists", "Help", "Settings", "Liked Song Statistics", "Save and Exit"};
            switch (listOptionsWithExit(options)) {
                case 0 -> streamingStatistics();
                case 1 -> streamingCharts();
                case 2 -> playlistCreation();
                case 3 -> playlistViewing();
                case 4 -> help();
                case 5 -> settings();
                case 6 -> likedSongStats();
                default -> {
                    saveSpotifyAnalysis();
                    System.exit(0);
                }
            }
        }
    }

    //------------------------------------------------UI BRANCHES------------------------------------------------
    //STREAMING Statistics AND HELPERS
    private static void streamingStatistics() {
        boolean notDone = true;
        while (notDone) {
            printTitle("Streaming Statistics:");
            switch (listOptions(new String[]{"Print Streaming History Overview", "Find First Occurrence Of A Track", "Find First Occurrence Of An Album", "Find First Occurrence Of An Artist","Return To Menu"})) {
                case 0 -> {printTitle(""); spotifyAnalysis.printStreamingInfo();}
                case 1 -> {
                    Date date = yesOrNo("Would you like to only consider tracks after a certain date?")?askForDate(true):null;
                    String artist = forceExistingArtist();
                    if (artist==null) break;
                    String track = forceExistingTrack(artist);
                    SpotifyStream s = spotifyAnalysis.getFirstTrackOccurrence(track,artist,date);
                    if (s==null) nullResponse(track,date); else System.out.println(s.track + " was heard first on " + s.date.simpleToString());
                }
                case 2 -> {
                    Date date = yesOrNo("Would you like to only consider tracks after a certain date?")?askForDate(true):null;
                    String artist = forceExistingArtist();
                    if (artist==null) break;
                    String album = forceExistingArtistAlbum(artist);
                    SpotifyStream s = spotifyAnalysis.getFirstAlbumOccurrence(album,artist,date);
                    if (s==null) nullResponse(album,date); else System.out.println(s.album + " was heard first on " + s.date.simpleToString() + " with the track: " + s.track);
                }
                case 3 -> {
                    Date date = yesOrNo("Would you like to only consider tracks after a certain date?")?askForDate(true):null;
                    String artist = forceExistingArtist();
                    if (artist==null) break;
                    SpotifyStream s = spotifyAnalysis.getFirstArtistOccurrence(artist,date);
                    if (s==null) nullResponse(artist,date); else System.out.println(s.artist + " was heard first on " + s.date.simpleToString() + " first with the track: " + s.track);
                }
                default -> notDone = false;
            }
        }
    }

    //STREAMING CHARTS AND HELPERS
    private static void streamingCharts() {
        boolean notDone = true;
        while (notDone) {
            printTitle("Streaming Charts:");
            switch (listOptions(new String[]{"Print Top Tracks", "Print Top Albums", "Print Top Artists", "Print Top Podcast Episodes", "Print Top Podcasts", "Return To Menu"})) {
                case 0 -> printTopTracks();
                case 1 -> printTopAlbums();
                case 2 -> printTopArtists();
                case 3 -> printTopEpisodes();
                case 4 -> printTopPodcasts();
                default -> notDone = false;
            }
        }
    }
    private static void printTopTracks() {
        System.out.println("Choose track grouping:");
        switch(listOptions(new String[]{"All Tracks","Tracks From An Album","Tracks By An Artist","Tracks In Date Range", "Tracks From An Album In Date Range", "Tracks By An Artist In Date Range"})) {
            case 0 -> System.out.println(SpotifyAnalysis.sortTopTracks(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency():spotifyAnalysis.getStreamPlaytime(),streamCount()));
            case 1 -> System.out.println(SpotifyAnalysis.sortTopTracks(frequencyOrPlaytime()?spotifyAnalysis.getAlbumStreamFrequency(forceExistingAlbum()):spotifyAnalysis.getAlbumStreamPlaytime(forceExistingAlbum())));
            case 2 -> System.out.println(SpotifyAnalysis.sortTopTracks(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency(forceExistingArtist()):spotifyAnalysis.getStreamPlaytime(forceExistingArtist()),streamCount()));
            case 3 -> System.out.println(SpotifyAnalysis.sortTopTracks(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency(askForDate(true),askForDate(false)):spotifyAnalysis.getStreamPlaytime(askForDate(true),askForDate(false)),streamCount()));
            case 4 -> System.out.println(SpotifyAnalysis.sortTopTracks(frequencyOrPlaytime()?spotifyAnalysis.getAlbumStreamFrequency(askForDate(true),askForDate(false),forceExistingAlbum()):spotifyAnalysis.getAlbumStreamPlaytime(askForDate(true),askForDate(false),forceExistingAlbum())));
            case 5 -> System.out.println(SpotifyAnalysis.sortTopTracks(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency(askForDate(true),askForDate(false),forceExistingArtist()):spotifyAnalysis.getStreamPlaytime(askForDate(true),askForDate(false),forceExistingArtist()),streamCount()));
        }
    }
    private static void printTopAlbums() {
        System.out.println("Choose album grouping:");
        switch(listOptions(new String[]{"All Albums","Albums By An Artist","Albums In Date Range","Albums By An Artist In Date Range"})) {
            case 0 -> System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getAlbumFrequency():spotifyAnalysis.getAlbumPlaytime(),streamCount()));
            case 1 -> System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getAlbumFrequency(forceExistingArtist()):spotifyAnalysis.getAlbumPlaytime(forceExistingArtist())));
            case 2 -> System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getAlbumFrequency(askForDate(true),askForDate(false)):spotifyAnalysis.getAlbumPlaytime(askForDate(true),askForDate(false)),streamCount()));
            case 3 -> System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getAlbumFrequency(askForDate(true),askForDate(false),forceExistingArtist()):spotifyAnalysis.getAlbumPlaytime(askForDate(true),askForDate(false),forceExistingArtist())));
        }
    }
    private static void printTopArtists() {
        if (yesOrNo("Would you like to only consider artists in a date range?"))
            System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getArtistFrequency(askForDate(true),askForDate(false)):spotifyAnalysis.getArtistPlaytime(askForDate(true),askForDate(false)),streamCount()));
        else
            System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getArtistFrequency():spotifyAnalysis.getArtistPlaytime(),streamCount()));
    }
    private static void printTopEpisodes() {
        System.out.println("Choose track grouping:");
        switch(listOptions(new String[]{"All Episodes","Episodes From A Podcast","Episodes In Date Range","Episodes From A Podcast In Date Range"})) {
            case 0 -> System.out.println(SpotifyAnalysis.sortTopEpisodes(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency():spotifyAnalysis.getStreamPlaytime(),streamCount()));
            case 1 -> System.out.println(SpotifyAnalysis.sortTopEpisodes(frequencyOrPlaytime()?spotifyAnalysis.getPodcastStreamFrequency(forceExistingPodcast()):spotifyAnalysis.getPodcastStreamPlaytime(forceExistingPodcast()),streamCount()));
            case 2 -> System.out.println(SpotifyAnalysis.sortTopEpisodes(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency(askForDate(true),askForDate(false)):spotifyAnalysis.getStreamPlaytime(askForDate(true),askForDate(false)),streamCount()));
            case 3 -> System.out.println(SpotifyAnalysis.sortTopEpisodes(frequencyOrPlaytime()?spotifyAnalysis.getPodcastStreamFrequency(askForDate(true),askForDate(false),forceExistingPodcast()):spotifyAnalysis.getPodcastStreamPlaytime(askForDate(true),askForDate(false),forceExistingPodcast()),streamCount()));
        }
    }
    private static void printTopPodcasts() {
        if (yesOrNo("Would you like to only consider podcasts in a date range?"))
            System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getPodcastFrequency(askForDate(true),askForDate(false)):spotifyAnalysis.getPodcastPlaytime(askForDate(true),askForDate(false)),streamCount()));
        else
            System.out.println(SpotifyAnalysis.sortTop(frequencyOrPlaytime()?spotifyAnalysis.getPodcastFrequency():spotifyAnalysis.getPodcastPlaytime(),streamCount()));
    }
    private static boolean frequencyOrPlaytime() {
        System.out.println("Would you like results to be sorted based on stream frequency or total playtime? (f/p)");
        String line = scanner.nextLine();
        while (!line.equalsIgnoreCase("f")&!line.equalsIgnoreCase("p")) {
            System.out.println("Please provide a valid response");
            line = scanner.nextLine();
        }
        return line.equalsIgnoreCase("f");
    }
    private static Date askForDate(boolean startingOrEnding) {
        if (startingOrEnding) System.out.println("Please enter starting date: (MM/DD/YYYY)");
        else System.out.println("Please enter ending date: (MM/DD/YYYY)");
        return forceDateNullAllowed();
    }
    private static int streamCount() {
        System.out.println("How many entries would you like to be included in the results?");
        return forcePositiveIntResponse();
    }

    //PLAYLIST CREATION
    private static void playlistCreation() {
        boolean notDone = true;
        List<SpotifyStream> playlist = null;
        while (notDone) {
            printTitle("Playlist Creation");
            switch (listOptions(new String[]{"Streaming Charts", "Hidden Gems", "Great Random Playlist", "Exit"})) {
                case 0: playlist = getTopTracks();
                case 1: playlist = hiddenGems(100);
                default: notDone = false;
            }
            if (playlist!=null) {
                if (yesOrNo("Would you like to view the current playlist?")) {
                    System.out.println(playlistToString(playlist, false));
                }
                if (yesOrNo("Would you like to add playlist to clipboard (Warning: Clipboard will be reset)")) {
                    copyToClipboard(playlistToString(playlist, true));
                }
            }
        }
    }
    private static LinkedList<SpotifyStream> getTopTracks() {
        LinkedList<SpotifyStream> playlist = null;
        System.out.println("Choose track grouping:");
        switch(listOptions(new String[]{"All Tracks","Tracks By An Artist","Tracks In Date Range", "Tracks From An Album In Date Range", "Tracks By An Artist In Date Range"})) {
            case 0 -> playlist = SpotifyAnalysis.sortPlaylist(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency():spotifyAnalysis.getStreamPlaytime(),streamCount());
            case 1 -> playlist = SpotifyAnalysis.sortPlaylist(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency(forceExistingArtist()):spotifyAnalysis.getStreamPlaytime(forceExistingArtist()),streamCount());
            case 2 -> playlist = SpotifyAnalysis.sortPlaylist(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency(askForDate(true),askForDate(false)):spotifyAnalysis.getStreamPlaytime(askForDate(true),askForDate(false)),streamCount());
            case 3 -> playlist = SpotifyAnalysis.sortPlaylist(frequencyOrPlaytime()?spotifyAnalysis.getStreamFrequency(askForDate(true),askForDate(false),forceExistingArtist()):spotifyAnalysis.getStreamPlaytime(askForDate(true),askForDate(false),forceExistingArtist()),streamCount());
        }
        return playlist;
    }
    private static LinkedList<SpotifyStream> hiddenGems(int n) {
        System.out.println("This creates a playlist not containing your top " + n + " artists");
        ArrayList<String> artists = SpotifyAnalysis.getSortedTop(spotifyAnalysis.getArtistFrequency(),n);
        return SpotifyAnalysis.sortPlaylist(spotifyAnalysis.getStreamFrequency(),streamCount(),artists);
    }
    private static String playlistToString(List<SpotifyStream> playlist, boolean uri) {
        StringBuilder output = new StringBuilder();
        for (SpotifyStream s: playlist) {
            if (uri) {
                output.append(s.spotify_track_uri).append("\n");
            }
            else {
                output.append(s.artist).append(": ").append(s.track);
            }
        }
        return output.toString();
    }

    //PLAYLIST VIEWING
    private static void playlistViewing() {

    }
    //HELP
    private static void help() {
        boolean notDone = true;
        while (notDone) {
            printTitle("Help:");
            switch(listOptions(new String[]{
                    "Obtaining Spotify Data",
                    "Playlist Creation/Viewing",
                    "Settings",
                    "Other",
                    "exit"
            })) {
                case 0 -> {
                    boolean notDone2 = true;
                    while (notDone2) {
                        switch (listOptions(new String[]{"Extended Streaming History", "Spotify Liked Songs", "Where to put data when received","Exit"})) {
                            case 0 -> System.out.println(
                                    "To request your Spotify Extended Streaming History, email Spotify at privacy@spotify.com and\n" +
                                    "request your extended streaming history. There will be a maximum 30 day wait for the requested\n" +
                                    "data as they prepare to send your information."
                            );
                            case 1 -> System.out.println(
                                    "Additional functionalities in Spotify Statistics and Playlist Creation are possible by utilizing information\n" +
                                    "from your liked song playlist from spotify. In order to access this data please visit the privacy settings of \n" +
                                    "your spotify account and begin step 1 to request the collection of your data"
                            );
                            case 2 -> System.out.println(
                                    "When Spotify emails you with the download for your spotify data, whether it is your extended streaming history or\n" +
                                    "your standard collection of streaming history, you must unzip and locate the relevant files for this program and copy\n" +
                                    "or move them to the SpotifyData folder in the program files. Current relevant files include the endsong_*.json files (there will\n" +
                                    "be multiple) from the requested extended streaming history, or the YourLibrary.json file from a Spotify data request."
                            );
                            default -> notDone2 = false;
                        }
                    }
                }
                case 1 -> {
                    boolean notDone2 = true;
                    while (notDone2) {
                        switch (listOptions(new String[]{"How to add playlists to Spotify", "Where to view saved playlists outside of program", "Exit"})) {
                            case 0 -> System.out.println(
                                    "To add playlists to Spotify without manually entering in songs, the desktop version of Spotify is required. Simply\n" +
                                    "name and create a new playlist on Spotify, then either using the software or by accessing the SpotifyUri folder for\n" +
                                    "saved playlists copy the list of spotifyURIs to your clipboard. Following this click on the desired playlist where songs\n" +
                                    "are or would be listed and use Control-V to paste all the songs directly into the playlist"
                            );
                            case 1 -> System.out.println(
                                    "Playlists can both be viewed and added to Spotify outside of this software by accessing internal files. Simply view the \"Playlists\"\n" +
                                    "folder within the program files and then either choose to access the \"Viewable\" directory of playlists which simply has the list of each\n" +
                                    "song in a playlist. Or access the \"SpotifyUri\" directory to copy and paste spotifyUri links into the Spotify desktop application to edit playlists."
                            );
                            default -> notDone2 = false;
                        }
                    }
                }
                case 2 -> {
                    boolean notDone2 = true;
                    while (notDone2) {
                        switch (listOptions(new String[]{"Overview", "What is a skipped stream?", "What is a song comparison setting", "What is the divisor", "Exit"})) {
                            case 0 -> System.out.println(
                                    "Settings include filters and other controls over your user experience. These filters allow you to exclude specific kinds of streams from consideration\n" +
                                    "in streaming statistics, chart making, or playlist creation. You can see how many streams have been filtered from the total in \"View Streaming History Overview.\""
                            );
                            case 1 -> System.out.println(
                                    "A skipped stream is any stream that has occurred where the stream ended for any other reason other than the song ending. Most cases of this\n" +
                                    "occur when the stream is skipped before finishing. Other cases involve when a person listens to but does not finish a song or when spotify\n" +
                                    "has an error."
                            );
                            case 2 -> System.out.println(
                                    "Within this software an internal list of every stream, however when streams are counted for making charts or playlists, smaller lists are created\n" +
                                    "that are able to have different settings for how to differentiate different streams. These settings include: [track], [track,artist],[track,album,artist],\n" +
                                    "and [spotifyUri]. It is important to understand that under these rules, identical streams cannot exist in the same playlist. As a result, when making playlists,\n" +
                                    "where spotifyUri links are very important, you have the option to remove the possibility of duplicate tracks (because often there are multiple versions of the same\n" +
                                    "song) on the basis of name, artist name, and/or album name. This gives you the option to create playlists that cannot have duplicate track names."
                            );
                            case 3 -> System.out.println(
                                    "The divisor is a value that changes the unit of time that Spotify data is reported in. The options include: milliseconds, seconds, minutes, hours, and days."
                            );
                            default -> notDone2 = false;
                        }
                    }
                }
                case 3 -> {
                    boolean notDone2 = true;
                    while (notDone2) {
                        switch (listOptions(new String[]{"Frequency vs Playtime", "Exit"})) {
                            case 0 -> System.out.println(
                                    "Song frequency refers to the number of times a song has been played, if referring to an artist or album, it the number of streams by or in the specified\n" +
                                    "artist or album. Song Playtime operates similarly but refers to the number of miliseconds or unit of time a song, album, or artist has been played."
                            );
                            default -> notDone2 = false;
                        }
                    }
                }
                default -> notDone = false;
            }
        }
    }
    //SETTINGS AND HELPERS
    private static void settings() {
        boolean notDone = true;
        while (notDone) {
            printTitle("Settings");
            switch (listOptions(new String[]{"Set Minimum Listen Time", "Set Maximum Listen Time", "Set Minimum Date",
                    "Set Maximum Date", "Filter Skipped Streams", "Include Only Music",
                    "Include Only Podcasts", "Include Only Online Streams", "Include Only Offline Streams",
                    "Set Song Comparison Setting", "Set Playtime Divisor", "Result All Settings To Default","Return To Menu"})) {
                case 0 -> spotifyAnalysis.setMinimumListenTime(Math.max(0,setIntSettings("minimum listen time", "0", spotifyAnalysis.getMinimumListenTime())));
                case 1 -> spotifyAnalysis.setMaximumListenTime(Math.max(-1,setIntSettings("maximum listen time", "-1", spotifyAnalysis.getMaximumListenTime())));
                case 2 -> spotifyAnalysis.setMinimumDate(setDateSettings("minimum date", spotifyAnalysis.getMinimumDate()));
                case 3 -> spotifyAnalysis.setMaximumDate(setDateSettings("maximum date", spotifyAnalysis.getMaximumDate()));
                case 4 -> spotifyAnalysis.setNoSkips(yesOrNo(booleanSettingsPrompt("include only un-skipped streams","no",spotifyAnalysis.getNoSkips())));
                case 5 -> spotifyAnalysis.setMusicOnly(yesOrNo(booleanSettingsPrompt("include only music","no",spotifyAnalysis.getMusicOnly())));
                case 6 -> spotifyAnalysis.setPodcastsOnly(yesOrNo(booleanSettingsPrompt("include only podcasts","no",spotifyAnalysis.getPodcastsOnly())));
                case 7 -> spotifyAnalysis.setOnlineOnly(yesOrNo(booleanSettingsPrompt("include only music streamed online","no",spotifyAnalysis.getOnlineOnly())));
                case 8 -> spotifyAnalysis.setOfflineOnly(yesOrNo(booleanSettingsPrompt("include only music streamed offline","no",spotifyAnalysis.getOfflineOnly())));
                case 9 -> {
                    printTitle("Please select a song comparison setting: (Default is " +
                            songComparisonToString(SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK_ARTIST) +
                            ", Current is " + songComparisonToString(spotifyAnalysis.getSpotifyStreamComparatorSetting()) + ")");
                    switch(listOptions(new String[]{"Compare Based On Spotify URI", "Compare Based On Track Name",
                            "Compare Based On Track And Artist Name", "Compare Based On Track, Album, And Artist Name"})) {
                        case 0 -> spotifyAnalysis.setSpotifyStreamComparator(SpotifyAnalysis.SpotifyStreamComparator.COMPARE_URI);
                        case 1 -> spotifyAnalysis.setSpotifyStreamComparator(SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK);
                        case 2 -> spotifyAnalysis.setSpotifyStreamComparator(SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK_ARTIST);
                        case 3 -> spotifyAnalysis.setSpotifyStreamComparator(SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK_ALBUM_ARTIST);
                    }
                }
                case 10 -> {
                    printTitle("Please select a playtime divisor: (Default is Minutes" +
                            ", Current is " + divisorToString(spotifyAnalysis.getDivider()) + ")");
                    switch(listOptions(new String[]{"Milliseconds","Seconds","Minutes","Hours","Days"})) {
                        case 0 -> spotifyAnalysis.setDivider(1);
                        case 1 -> spotifyAnalysis.setDivider(1000);
                        case 2 -> spotifyAnalysis.setDivider(60000);
                        case 3 -> spotifyAnalysis.setDivider(3600000);
                        case 4 -> spotifyAnalysis.setDivider(86400000);
                    }
                }
                case 11 -> {
                    spotifyAnalysis.setMinimumListenTime(0);
                    spotifyAnalysis.setMaximumListenTime(-1);
                    spotifyAnalysis.setMinimumDate(null);
                    spotifyAnalysis.setMaximumDate(null);
                    spotifyAnalysis.setNoSkips(false);
                    spotifyAnalysis.setMusicOnly(false);
                    spotifyAnalysis.setPodcastsOnly(false);
                    spotifyAnalysis.setOnlineOnly(false);
                    spotifyAnalysis.setOfflineOnly(false);
                    spotifyAnalysis.setDivider(60000);
                    spotifyAnalysis.setSpotifyStreamComparator(SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK_ARTIST);
                }
                default -> notDone = false;
            }
        }
        System.out.println("Loading.. ");
        refreshSpotifyAnalysis();
    }
    private static int setIntSettings(String setting, String def, int current) {
        System.out.println("Please choose a new " + setting + ": (Default is " + def + ", Current is " + current + ")");
        return forceIntResponse();
    }
    private static Date setDateSettings(String setting, Date current) {
        System.out.println("Please choose a new " + setting + ": (Default is " + "null" + ", Current is " + current + ")");
        return forceDateNullAllowed();
    }
    private static String booleanSettingsPrompt(String setting, String def, boolean current) {
        return "Would you like to " + setting + ": (Default is " + def + ", Current is " + (current?"yes":"no") + ")";
    }
    private static String songComparisonToString(int i) {
        if (i == SpotifyAnalysis.SpotifyStreamComparator.COMPARE_URI) return "Spotify URI";
        else if (i == SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK) return "Track";
        else if (i == SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK_ARTIST) return "Track and Artist";
        else if (i == SpotifyAnalysis.SpotifyStreamComparator.COMPARE_TRACK_ALBUM_ARTIST) return "Track, Album, and Artist";
        else return "invalid";
    }
    private static String divisorToString(int i) {
        if (i==1) return "Milliseconds";
        if (i==1000) return "Seconds";
        if (i==60000) return "Minutes";
        if (i==3600000) return "Hours";
        if (i==86400000) return "Days";
        else return "ERROR";
    }
    //LIKED SONG STATS AND HELPERS
    private static void likedSongStats() {
        boolean notDone = true;
        while (notDone) {
            printTitle("Liked Songs Statistics:");
            System.out.println("You have: " + spotifyAnalysis.likedSongCount() + " recorded liked songs");
            switch (listOptions(new String[]{"Artist Percentage and Count", "Artist Ranking", "Exit"})) {
                case 0 -> {
                    String artist = forceExistingLikedArtist();
                    int count = spotifyAnalysis.artistCount(artist);
                    System.out.println("You have " + count + " liked songs by " + artist);
                    double percentage = ((int)(((double)count/spotifyAnalysis.likedSongCount())*10000))/100.0;
                    System.out.println(percentage + "% of your liked songs are by " + artist);
                }
                case 1 -> {
                    
                }
                default -> notDone = false;
            }
        }
    }
    private static String forceExistingLikedArtist() {
        if (spotifyAnalysis.likedSongCount()==0) {
            System.out.println("No Streams Found");
            return null;
        }
        System.out.println("Please name an artist: ");
        String line = scanner.nextLine();
        while (!spotifyAnalysis.likedSongsHaveArtist(line)) {
            System.out.println("Please choose a liked artist");
            line = scanner.nextLine();
        }
        return line;
    }

    //FORCING EXISTING DATA + RECOMMENDATIONS
    private static String forceExistingTrack(String artist) {
        if (spotifyAnalysis.getArtistCount()==0) {
            System.out.println("No Streams Found");
            return null;
        }
        System.out.println("Please name a track: ");
        String line = scanner.nextLine();
        while (!spotifyAnalysis.trackExists(line,artist)) {
            System.out.println("Please choose a streamed track by the artist");
            line = scanner.nextLine();
        }
        return line;
    }
    private static String forceExistingAlbum() {
        if (spotifyAnalysis.getArtistCount()==0) {
            System.out.println("No Streams Found");
            return null;
        }
        System.out.println("Please name an Album: ");
        String line = scanner.nextLine();
        while (!spotifyAnalysis.albumExists(line)) {
            System.out.println("Please choose a streamed album");
            line = scanner.nextLine();
        }
        return line;
    }
    private static String forceExistingArtistAlbum(String artist) {
        if (spotifyAnalysis.getArtistCount()==0) {
            System.out.println("No Streams Found");
            return null;
        }
        System.out.println("Please name an album by " + artist + ": ");
        String line = scanner.nextLine();
        while (!spotifyAnalysis.albumExists(line) || !spotifyAnalysis.albumByArtist(line,artist)) {
            System.out.println("Please choose a streamed album by " + artist);
            line = scanner.nextLine();
        }
        return line;
    }
    private static String forceExistingArtist() {
        if (spotifyAnalysis.getArtistCount()==0) {
            System.out.println("No Streams Found");
            return null;
        }
        System.out.println("Please name an artist: ");
        String line = scanner.nextLine();
        while (!spotifyAnalysis.artistExists(line)) {
            System.out.println("Please choose a streamed artist");
            line = scanner.nextLine();
        }
        return line;
    }
    private static String forceExistingPodcast() {
        if (spotifyAnalysis.getPodcastCount()==0) {
            System.out.println("No Streams Found");
            return null;
        }
        System.out.println("Please name a podcast: ");
        String line = scanner.nextLine();
        while (!spotifyAnalysis.podcastExists(line)) {
            System.out.println("Please choose a streamed podcast");
            line = scanner.nextLine();
        }
        return line;
    }

    //General UI HELPERS
    private static void nullResponse(String s, Date d) {
        System.out.println(s + " has not been listened to after " + d.simpleToString());
    }
    private static int listOptions(String[] list) { //Returns index of chosen option, guarantees valid index
        for (int i = 0;i<list.length;i++) System.out.println(i+1 + ": " + list[i]);
        int next = forceIntResponse();
        while (next<1|next>list.length) {
            System.out.println("Please provide a valid response");
            next = forceIntResponse();
        }
        return next-1;
    }
    private static int listOptionsWithExit(String[] list) { //Returns index of chosen option, guarantees valid index
        for (int i = 0;i<list.length;i++) System.out.println(i+1 + ": " + list[i]);
        int next = forceIntResponse();
        while (next<1|next>list.length) {
            System.out.println("Please provide a valid response");
            next = forceIntResponse();
        }
        if (next==list.length) return -1;
        return next-1;
    }
    private static boolean yesOrNo(String prompt) {
        System.out.println(prompt + " (y/n)");
        String line = scanner.nextLine();
        while (!line.equalsIgnoreCase("y")&!line.equalsIgnoreCase("n")&!line.equalsIgnoreCase("yes")&!line.equalsIgnoreCase("no")) {
            System.out.println("Please provide a valid response");
            line = scanner.nextLine();
        }
        return line.equalsIgnoreCase("y")|line.equalsIgnoreCase("yes");
    }
    private static int forceIntResponse() {
        String line = scanner.nextLine();
        while (!stringIsInteger(line)) {
            System.out.println("Please provide a valid response");
            line = scanner.nextLine();
        }
        return Integer.parseInt(line);
    }
    private static int forcePositiveIntResponse() {
        int n = forceIntResponse();
        while (n<0) {
            System.out.println("Please provide a valid response");
            n = forceIntResponse();
        }
        return n;
    }
    private static boolean stringIsInteger(String s) {
        if (s == null) return false;
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
    private static Date forceDate() {
        String[] date = scanner.nextLine().split("/");
        while (date.length != 3 || !(isValidNumber(date[0], 1, 12) && isValidNumber(date[1], 1, 31) && isValidNumber(date[2], 1000, 10000))) {
            System.out.println("Please provide a valid response");
            date = scanner.nextLine().split("/");
        }
        return new Date(Integer.parseInt(date[2]),Integer.parseInt(date[0]),Integer.parseInt(date[1]));
    }
    private static Date forceDateNullAllowed() {
        String s = scanner.nextLine();
        if (s.equalsIgnoreCase("null") | s.equals("")) return null;
        String[] date = s.split("/");
        while (date.length != 3 || !(isValidNumber(date[0], 1, 12) && isValidNumber(date[1], 1, 31) && isValidNumber(date[2], 1000, 10000))) {
            System.out.println("Please provide a valid response");
            s = scanner.nextLine();
            if (s.equalsIgnoreCase("null")) return null;
            date = s.split("/");
        }
        return new Date(Integer.parseInt(date[2]),Integer.parseInt(date[0]),Integer.parseInt(date[1]));
    }
    private static boolean isValidNumber(String s, int min, int max) {
        if (!stringIsInteger(s))return false;
        int n = Integer.parseInt(s);
        return n>=min & n<=max;
    }
    private static void copyToClipboard(String s) {
        StringSelection stringSelection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    //SAVE STATES
    private static void retrieveSpotifyAnalysis() {
        File f = new File("MySpotifyAnalysis.ser");
        if (f.exists()) {
            try {
                FileInputStream file_in = new FileInputStream(f);
                ObjectInputStream obj_in = new ObjectInputStream(file_in);
                Object obj = obj_in.readObject();
                if (obj instanceof SpotifyAnalysis) spotifyAnalysis = (SpotifyAnalysis) obj;
                file_in.close();
                obj_in.close();
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        else {
            if (containsValidSpotifyData()) {
                File library = getLibrary();
                if (library == null) spotifyAnalysis = new SpotifyAnalysis(endSongFiles(spotifyDataFiles()));
                else spotifyAnalysis = new SpotifyAnalysis(endSongFiles(spotifyDataFiles()), library);
            }
            else {
                System.out.println("No valid spotify data located. Please put endsong_*.json and yourLibrary.json files in the SpotifyData directory in program files");
                System.exit(0);
            }
        }
    }
    private static void refreshSpotifyAnalysis() {
        spotifyAnalysis.loadStreamingHistory(spotifyDataFiles());
        saveSpotifyAnalysis();
    }
    private static void saveSpotifyAnalysis() {
        try {
            FileOutputStream file_out = new FileOutputStream("MySpotifyAnalysis.ser");
            ObjectOutputStream obj_out = new ObjectOutputStream(file_out);
            obj_out.writeObject(spotifyAnalysis);
            file_out.close();
            obj_out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //HELPER METHODS
    private static void printTitle(String s) {
        System.out.print(s);
        for (int i = 0;i<titleLength-s.length();i++) {
            System.out.print("-");
        }
        System.out.println();
    }
    private static File[] spotifyDataFiles() {
        File f = new File("SpotifyData");
        if (!f.isDirectory()) {
            System.out.println("SpotifyData directory missing from program files");
            System.exit(-1);
        }
        return f.listFiles();
    }
    private static boolean containsValidSpotifyData() {
        return endSongFiles(spotifyDataFiles()).length>0;
    }
    private static File[] endSongFiles(File[] arr) {
        ArrayList<File> endSongs = new ArrayList<>();
        for (File f: arr) {
            if (f.getName().startsWith("Streaming_History_Audio_")) endSongs.add(f);
        }
        return endSongs.toArray(new File[0]);
    }
    private static File getLibrary() {
        for (File f: spotifyDataFiles()) {
            if (f.getName().equals("YourLibrary.json")) return f;
        }
        return null;
    }
}
