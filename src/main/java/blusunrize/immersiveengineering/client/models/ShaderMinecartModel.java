/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.entity.model.MinecartModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.ModelBox;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ShaderMinecartModel<T extends MinecartEntity> extends MinecartModel<T>
{
	public static Int2ObjectMap<ItemStack> shadedCarts = new Int2ObjectOpenHashMap<>();
	public static boolean rendersReplaced = false;

	public List<RendererModel> sideModelsMirrored;

	public ShaderMinecartModel(MinecartModel<T> model)
	{
		super();
		this.boxList.clear();
		this.boxList.addAll(ClientUtils.copyModelRenderers(model, model.boxList));
		this.sideModelsMirrored = ClientUtils.copyModelRenderers(model, model.boxList);
		sideModelsMirrored.get(4).mirror = true;
		ArrayList<ModelBox> newCubes = new ArrayList<ModelBox>();
		for(ModelBox cube : sideModelsMirrored.get(4).cubeList)
			newCubes.add(new ModelBox(sideModelsMirrored.get(4), 0, 0, cube.posX1, cube.posY1, cube.posZ1, (int)(cube.posX2-cube.posX1), (int)(cube.posY2-cube.posY1), (int)(cube.posZ2-cube.posZ1), 0));
		sideModelsMirrored.get(4).cubeList.clear();
		sideModelsMirrored.get(4).cubeList.addAll(newCubes);
	}

	@Override
	public void render(T entity, float f0, float f1, float f2, float f3, float f4, float f5)
	{
		ShaderCase sCase = null;
		ItemStack shader = ItemStack.EMPTY;
		if(shadedCarts.containsKey(entity.getEntityId()))
		{
			shader = shadedCarts.get(entity.getEntityId());
			if(shader!=null&&!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, null, new ResourceLocation(ImmersiveEngineering.MODID, "minecart"));
		}
		if(sCase!=null)
		{
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 0, 1);

			boxList.get(5).rotationPointY = 4.0F-f2;
			sideModelsMirrored.get(5).rotationPointY = 4.0F-f2;
			for(int part = 0; part < boxList.size()-1; part++)
				if(boxList.get(part)!=null)
				{
					float scale = 1;
					ShaderLayer[] layers = sCase.getLayers();

					//identify part 1+2, they shouldn'T render with additional?!

					for(int pass = 0; pass < layers.length; pass++)
						if(sCase.renderModelPartForPass(shader, null, ""+part, pass))
						{
							int col = sCase.getARGBColourModifier(shader, null, ""+part, pass);
							GlStateManager.scalef(scale, scale, scale);
							GlStateManager.color4f((col >> 16&255)/255f, (col >> 8&255)/255f, (col&255)/255f, (col >> 24&255)/255f);

							ClientUtils.mc().getTextureManager().bindTexture(sCase.getReplacementSprite(shader, null, ""+part, pass));

							if(layers[pass].isDynamicLayer())
								layers[pass].modifyRender(true, part);
							if(((ShaderCaseMinecart)sCase).mirrorSideForPass[pass])
								sideModelsMirrored.get(part).render(f5);
							else
								boxList.get(part).render(f5);
							if(layers[pass].isDynamicLayer())
								layers[pass].modifyRender(false, part);

							GlStateManager.color3f(1, 1, 1);
							GlStateManager.scalef(1/scale, 1/scale, 1/scale);
						}
				}

			GlStateManager.disableBlend();
		}
		else
			super.render(entity, f0, f1, f2, f3, f4, f5);
	}
}