package exposed.hydrogen.playermodel;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import exposed.hydrogen.playermodel.model.PlayerMob;
import exposed.hydrogen.resources.CustomResource;
import exposed.hydrogen.resources.Resources;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extensions.Extension;
import net.worldseed.multipart.ModelEngine;
import net.worldseed.multipart.parser.ModelParser;
import net.worldseed.multipart.parser.generator.ModelGenerator;
import org.apache.commons.io.FileUtils;
import team.unnamed.creative.base.Writable;

import javax.naming.SizeLimitExceededException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;

public final class Player_Model extends Extension {
    public static final String NAMESPACE = "playermodels";
    private static Path BASE_PATH;
    private static Path MODEL_PATH;
    private static Path TEMP_PATH;
    @Getter private static PlayerMob currentMob;
    @Getter private static Player_Model instance;

    @Override
    public void initialize() {
        instance = this;
        this.getDataDirectory().toFile().mkdirs();
        BASE_PATH = getInstance().getDataDirectory();
        MODEL_PATH = getInstance().getDataDirectory().resolve("models");
        TEMP_PATH = getInstance().getDataDirectory().resolve("temp");

        MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerMob mob = new PlayerMob(player.getInstance(), player.getPosition(), player);
            mob.spawn();
            currentMob = mob;
        });

        MinecraftServer.getCommandManager().register(new AnimationCommand());

        Gson gson = new Gson();
        InputStreamReader reader = new InputStreamReader(this.getClass().getResourceAsStream("/playermodel.bbmodel"));
        ModelGenerator.BBEntityModel playerModel = ModelGenerator.generate(
                gson.fromJson(reader, JsonObject.class),
                "playermodel"
        );

        try {
            FileUtils.writeStringToFile(new File(MODEL_PATH.resolve(playerModel.id()) + "/model.animation.json"), playerModel.animations().toString(), Charset.defaultCharset());
            FileUtils.writeStringToFile(new File(MODEL_PATH.resolve(playerModel.id()) + "/model.geo.json"), playerModel.geo().toString(), Charset.defaultCharset());
            FileUtils.writeByteArrayToFile(new File(MODEL_PATH.resolve(playerModel.id()) + "/texture.png"), playerModel.textures().get("0"));

            ModelParser.parse(TEMP_PATH,MODEL_PATH,TEMP_PATH);
            ModelEngine.loadMappings(TEMP_PATH.resolve("model_mappings.json"),MODEL_PATH);
            List<CustomResource> resources = new LinkedList<>();
            List<Path> walkedTemp = Files.walk(TEMP_PATH).toList();
            List<Path> walkedMinecraft = Files.walk(BASE_PATH.resolve("minecraft")).toList();

            walkedTemp.forEach(path -> {
                if(!path.toFile().isFile()) {
                    return;
                }
                Writable writable = Writable.file(path.toFile());
                CustomResource customResource =
                        CustomResource.builder()
                                .data(writable)
                                .path(path.toString().replace(TEMP_PATH.toString(), "assets/wsee"))
                                .build();
                resources.add(customResource);
            });
            walkedMinecraft.forEach(path -> {
                if(!path.toFile().isFile()) {
                    return;
                }
                Writable writable = Writable.file(path.toFile());
                CustomResource customResource =
                        CustomResource.builder()
                                .data(writable)
                                .path(path.toString().replace(BASE_PATH.toString(), "assets"))
                                .build();
                resources.add(customResource);
            });
            Resources.getResourcePackHandler().addCustomResources(resources,true);
        } catch (SizeLimitExceededException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void terminate() {

    }
}
