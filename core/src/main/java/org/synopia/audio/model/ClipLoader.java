package org.synopia.audio.model;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v2FrameSet;
import com.mpatric.mp3agic.ID3v2TextFrameData;
import com.mpatric.mp3agic.Mp3File;

import java.io.IOException;
import java.util.Map;

/**
 * Created by synopia on 19.09.2015.
 */
public class ClipLoader {
    public Clip load(Track track) {
        open(track);
        Clip clip = new Clip();
        try {
            clip.load(track.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clip;
    }

    public void open(Track track) {
        try {
            Mp3File file = new Mp3File(track.getFilename());
            ID3v2 tag = file.getId3v2Tag();
            Map<String, ID3v2FrameSet> frameSets = tag.getFrameSets();
            ID3v2FrameSet frameSet = frameSets.get("TKEY");
            if (frameSet != null) {
                ID3v2TextFrameData var4 = new ID3v2TextFrameData(false, frameSet.getFrames().get(0).getData());
                String key = var4.getText().toString();
                track.setTagKey(Key.fromCamelot(key));
            }
            frameSet = frameSets.get("TBPM");
            if (frameSet != null) {
                ID3v2TextFrameData var4 = new ID3v2TextFrameData(false, frameSet.getFrames().get(0).getData());
                String bpm = var4.getText().toString();
                float tagBpm = Float.parseFloat(bpm);
                track.setTagBpm(tagBpm);
            }
            track.setArtist(tag.getArtist());
            track.setTitle(tag.getTitle());
            track.setLabel(tag.getPublisher());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
