package cn.wode490390.nukkit.ompop;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkPopulateEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.level.generator.populator.type.Populator;
import cn.nukkit.plugin.PluginBase;
import cn.wode490390.nukkit.ompop.populator.PopulatorOceanMonument;
import cn.wode490390.nukkit.ompop.scheduler.ChunkPopulationTask;
import cn.wode490390.nukkit.ompop.util.MetricsLite;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class OceanMonumentPlugin extends PluginBase implements Listener {

    public static final boolean DEBUG = false;

    private static OceanMonumentPlugin INSTANCE;

    private final Map<Level, List<Populator>> populators = Maps.newHashMap();

    @Override
    public void onLoad() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        try {
            new MetricsLite(this, 7728);
        } catch (Throwable ignore) {

        }

        PopulatorOceanMonument.init();

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onLevelLoad(LevelLoadEvent event) {
        List<Populator> populators = Lists.newArrayList();
        Level level = event.getLevel();
        Generator generator = level.getGenerator();
        if (generator.getId() != Generator.TYPE_FLAT && generator.getDimension() == Level.DIMENSION_OVERWORLD) {
            populators.add(new PopulatorOceanMonument());
        }
        this.populators.put(level, populators);
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
        Level level = event.getLevel();
        List<Populator> populators = this.populators.get(level);
        if (populators != null) {
            this.getServer().getScheduler().scheduleAsyncTask(this, new ChunkPopulationTask(level, event.getChunk(), populators));
        }
    }

    @EventHandler
    public void onLevelUnload(LevelUnloadEvent event) {
        this.populators.remove(event.getLevel());
    }

    public static OceanMonumentPlugin getInstance() {
        return INSTANCE;
    }

    public static void debug(Object... objs) {
        if (DEBUG) {
            try {
                StringJoiner joiner = new StringJoiner(" ");
                for (Object obj : objs) {
                    joiner.add(String.valueOf(obj));
                }
                INSTANCE.getLogger().warning(joiner.toString());
            } catch (Throwable ignore) {

            }
        }
    }
}
