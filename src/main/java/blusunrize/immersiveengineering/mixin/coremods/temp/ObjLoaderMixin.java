package blusunrize.immersiveengineering.mixin.coremods.temp;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.obj.ObjLoader;
import net.minecraftforge.client.model.obj.ObjMaterialLibrary;
import net.minecraftforge.client.model.obj.ObjModel;
import net.minecraftforge.client.model.obj.ObjModel.ModelSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = ObjLoader.class, remap = false)
public class ObjLoaderMixin
{
	@Shadow
	@Final
	@Mutable
	private Map<ModelSettings, ObjModel> modelCache;

	@Shadow
	@Final
	@Mutable
	private Map<ResourceLocation, ObjMaterialLibrary> materialCache;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void makeMapsSafe(CallbackInfo ci)
	{
		this.modelCache = new ConcurrentHashMap<>();
		this.materialCache = new ConcurrentHashMap<>();
	}
}
