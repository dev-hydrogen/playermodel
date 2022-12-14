package exposed.hydrogen.playermodel.model;

import net.worldseed.multipart.GenericModel;
import net.worldseed.multipart.animations.AnimationHandlerImpl;

import java.util.Map;

import static java.util.Map.entry;

public class PlayerModelAnimationHandler extends AnimationHandlerImpl {
    public static final Map<String, Integer> ANIMATION_PRIORITIES = Map.ofEntries(
            entry("animation.playermodel.doublewave", -2),
            entry("animation.playermodel.wave", -1),
            entry("animation.playermodel.dance.a", 0),
            entry("animation.playermodel.walk", 1),
            entry("animation.playermodel.idle", 2));

    public PlayerModelAnimationHandler(GenericModel model) {
        super(model);
    }

    @Override
    public Map<String, Integer> animationPriorities() {
        return ANIMATION_PRIORITIES;
    }
}
