package exposed.hydrogen.playermodel.model;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.worldseed.multipart.animations.AnimationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PlayerMob extends LivingEntity {
    private final PlayerModel model;
    private final AnimationHandler animationHandler;
    private final Player player;

    public PlayerMob(Instance instance, Pos pos, Player player) {
        super(EntityType.ZOMBIE);
        this.entityMeta.setInvisible(true);

        this.player = player;

        LivingEntity nametag = new LivingEntity(EntityType.ARMOR_STAND);
        nametag.setCustomNameVisible(true);
        nametag.setCustomName(player.getName());
        nametag.setNoGravity(true);
        nametag.setInvisible(true);
        nametag.setInstance(instance, pos);

        ArmorStandMeta meta = (ArmorStandMeta) nametag.getEntityMeta();
        meta.setMarker(true);

        this.model = new PlayerModel();
        model.init(instance, pos, this, nametag);

        this.animationHandler = new PlayerModelAnimationHandler(model);
        animationHandler.playRepeat("animation.playermodel.walk");

        setBoundingBox(1, 4, 1);
        this.setInstance(instance, pos);
        this.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.15f);

        // No way to set size without modifying minestom
        // PufferfishMeta meta = ((PufferfishMeta)this.getLivingEntityMeta());
        // meta.setSize(20);
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (this.isDead) {
            return;
        }
        this.model.setPosition(player.getPosition());
        this.model.setGlobalRotation(-player.getPosition().yaw());
    }

    @Override
    public void remove() {
        var viewers = Set.copyOf(this.getViewers());
        this.animationHandler.playOnce("animation.playermodel.death", (cb) -> {
            this.model.destroy();
            this.animationHandler.destroy();
            ParticlePacket packet = ParticleCreator.createParticlePacket(Particle.POOF, position.x(), position.y() + 1, position.z(), 1, 1, 1, 50);
            viewers.forEach(v -> v.sendPacket(packet));
        });

        super.remove();
    }

    @Override
    public @NotNull Set<Entity> getPassengers() {
        return model.getPassengers();
    }
}
