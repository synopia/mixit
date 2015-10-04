package org.synopia.audio.gdxui;

import org.synopia.audio.gdxui.table.DataTable;
import org.synopia.audio.model.ATrack;
import org.synopia.audio.model.TrackDatabase;

import java.util.List;

/**
 * Created by synopia on 27.09.2015.
 */
public class LibraryView extends DataTable<ATrack> {
    private TrackDatabase db;

    public LibraryView(TrackDatabase db) {
        this.db = db;

        List<ATrack> all = db.all();
        addColumn(new Column<ATrack, String>(150) {
            @Override
            public String stringValue(ATrack item) {
                return item.getLabel();
            }
        });
        addColumn(new Column<ATrack, String>(150) {
            @Override
            public String stringValue(ATrack item) {
                return item.getArtist();
            }
        });
        addColumn(new Column<ATrack, String>(150) {
            @Override
            public String stringValue(ATrack item) {
                return item.getTitle();
            }
        });
        addColumn(new Column<ATrack, String>(50) {
            @Override
            public String stringValue(ATrack item) {
                return Float.toString(item.getTagBpm());
            }
        });
        addColumn(new Column<ATrack, String>(50) {
            @Override
            public String stringValue(ATrack item) {
                return item.getTagKey() != null ? item.getTagKey().toString() : "-";
            }
        });
        addColumn(new Column<ATrack, String>(50) {
            @Override
            public String stringValue(ATrack item) {
                return Float.toString(item.getCalculatedBpm());
            }
        });
        addColumn(new Column<ATrack, String>(50) {
            @Override
            public String stringValue(ATrack item) {
                return item.getCalculatedKey() != null ? item.getCalculatedKey().toString() : "-";
            }
        });
        setRows(all);
    }
}
