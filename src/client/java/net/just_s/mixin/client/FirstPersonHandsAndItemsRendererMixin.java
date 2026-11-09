package net.just_s.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.just_s.PossessiveModClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class FirstPersonHandsAndItemsRendererMixin {

    // Hands and items are lit as if they were held by the possessed entity, not by the player.
    // lightCoords is the only int argument of submitArmWithItem, partialTicks is its first float.
    @ModifyVariable(
            method = "submitArmWithItem",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int possessive$replaceLightCoords(int lightCoords, @Local(argsOnly = true, ordinal = 0) float partialTicks) {
        if (PossessiveModClient.cameraHandler.isEnabled()) {
            return Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(
                    PossessiveModClient.cameraHandler.getCamera(), partialTicks
            );
        }
        return lightCoords;
    }
}
