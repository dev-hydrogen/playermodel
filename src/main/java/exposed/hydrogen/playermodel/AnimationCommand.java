package exposed.hydrogen.playermodel;

import exposed.hydrogen.playermodel.model.PlayerModelAnimationHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.worldseed.multipart.animations.AnimationHandler;

public class AnimationCommand extends Command {
    public AnimationCommand() {
        super("animate");

        Argument<String> stringArg = ArgumentType.String("animation").setSuggestionCallback((sender, context, suggestion) -> {
            PlayerModelAnimationHandler.ANIMATION_PRIORITIES.forEach((name, priority) -> {
                suggestion.addEntry(new SuggestionEntry(name, Component.text("highlight", NamedTextColor.LIGHT_PURPLE)));
            });
        }).setDefaultValue("animation.playermodel.dance.a");
        addSyntax((sender, context) -> {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Component.text("Please run this command in-game.", NamedTextColor.RED));
                return;
            }
            AnimationHandler handler = Player_Model.getCurrentMob()
                    .getAnimationHandler();
            handler.playOnce(context.get("animation"), cb -> {
                handler.playRepeat("animation.playermodel.idle");
            });
        }, stringArg);
    }
}
