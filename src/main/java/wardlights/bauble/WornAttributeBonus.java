package wardlights.bauble;

import java.util.UUID;
import java.util.function.DoubleSupplier;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

/**
 * One attribute bonus a bauble gives while worn, e.g. +3 attack damage. It's a
 * plain attribute modifier, so vanilla adds it to everything else on that
 * attribute: potions, beacons and other mods stack with it, and none of them
 * can remove or replace it.
 *
 * <p>The modifier isn't saved with the player. Instead the bauble calls
 * {@link #apply} from onWornTick, which puts it back after a login, a respawn or
 * a config change, and {@link #remove} from onUnequipped.
 *
 * <p>Operations, as vanilla numbers them: 0 adds to the base value, 1 adds a
 * percentage of the base, 2 multiplies the total (0.5 = x1.5).
 */
public class WornAttributeBonus {

    private final IAttribute attribute;
    /** Fixed forever: a modifier is found and removed by this id. */
    private final UUID id;
    private final String name;
    private final int operation;
    /** Read every time, so a config change applies without re-equipping. */
    private final DoubleSupplier amount;

    public WornAttributeBonus(IAttribute attribute, UUID id, String name, int operation, DoubleSupplier amount) {
        this.attribute = attribute;
        this.id = id;
        this.name = name;
        this.operation = operation;
        this.amount = amount;
    }

    /** Adds the bonus, or swaps it when the configured amount changed. Cheap enough to call every tick. Server only. */
    public void apply(EntityLivingBase wearer) {
        IAttributeInstance instance = wearer.getEntityAttribute(attribute);
        if (wearer.world.isRemote || instance == null) {
            return;
        }
        double wanted = amount.getAsDouble();
        AttributeModifier current = instance.getModifier(id);
        if (current != null && current.getAmount() == wanted) {
            return;
        }
        if (current != null) {
            instance.removeModifier(id);
        }
        if (wanted != 0) {
            instance.applyModifier(new AttributeModifier(id, name, wanted, operation).setSaved(false));
        }
    }

    /** Takes the bonus off; does nothing when it isn't there. Server only. */
    public void remove(EntityLivingBase wearer) {
        IAttributeInstance instance = wearer.getEntityAttribute(attribute);
        if (!wearer.world.isRemote && instance != null) {
            instance.removeModifier(id);
        }
    }

    public boolean isApplied(EntityLivingBase entity) {
        IAttributeInstance instance = entity.getEntityAttribute(attribute);
        return instance != null && instance.getModifier(id) != null;
    }
}
