package ai.onnxruntime.example.objectdetection;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import javax.net.SocketFactory;

public class RtspActivity extends AppCompatActivity {

    private PlayerView playerView;
    private SimpleExoPlayer player;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp);


        playerView = findViewById(R.id.player_view);
        player = new SimpleExoPlayer.Builder(this).build();

        // Create a data source factory.
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, "ai.onnxruntime.example.objectdetection")
        );

        // Create an RTSP media source pointing to your local LAN IP and port.
        //String rtspUrl = "rtsp://admin:admin@192.168.1.88:554";
        String rtspUrl = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mp4";
        RtspMediaSource mediaSource = new RtspMediaSource.Factory()
                .createMediaSource(MediaItem.fromUri(Uri.parse(rtspUrl)));

        // Prepare the player and set the media source.
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        // Attach the player to the PlayerView.
        playerView.setPlayer(player);

        /*
        PlayerView playerView = (PlayerView) findViewById(R.id.player_view);

        try {
            // Create an RTSP media source pointing to an RTSP uri.
            String rtspUri = "rtsp://admin:admin@192.168.1.88:554"; // rtsp://<username>:<password>@<host address>
            //RtspMediaSource mediaSource = new RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(rtspUri));
            // Create an RTSP media source pointing to an RTSP uri and override the socket factory.
            MediaSource mediaSource =
                    new RtspMediaSource.Factory()
                            .setSocketFactory(SocketFactory.getDefault().createSocket())
            .createMediaSource(MediaItem.fromUri(rtspUri));
            // Create a player instance.
            ExoPlayer player = new ExoPlayer.Builder(this).build();
            // Set the media source to be played.
            player.setMediaSource(mediaSource);
            // Prepare the player.
            player.prepare();
            playerView.setPlayer(player);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }
}