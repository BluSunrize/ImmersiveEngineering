/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ItemRendererIEOBJ extends TileEntityItemStackRenderer
{
	public static final TileEntityItemStackRenderer INSTANCE = new ItemRendererIEOBJ();
	private static FloatBuffer transform = GLAllocation.createDirectFloatBuffer(16);
	private static final Matrix4 mat = new Matrix4();

	@Override
	public void renderByItem(ItemStack stack, float partialTicks)
	{
		GlStateManager.enableCull();
		partialTicks = mc().getRenderPartialTicks();
		if(stack.getItem() instanceof IOBJModelCallback)
		{
			IOBJModelCallback<ItemStack> callback = (IOBJModelCallback<ItemStack>)stack.getItem();
			World w = IESmartObjModel.tempEntityStatic!=null?IESmartObjModel.tempEntityStatic.world: null;
			IBakedModel model = mc().getRenderItem().getItemModelWithOverrides(stack, w,
					IESmartObjModel.tempEntityStatic);
			if(model instanceof IESmartObjModel)
			{
				GlStateManager.disableCull();

				ItemStack shader = ItemStack.EMPTY;
				ShaderCase sCase = null;
				if(!stack.isEmpty()&&stack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
				{
					CapabilityShader.ShaderWrapper wrapper = stack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
					if(wrapper!=null)
					{
						shader = wrapper.getShaderItem();
						if(!shader.isEmpty()&&shader.getItem() instanceof IShaderItem)
							sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader, stack, wrapper.getShaderType());
					}
				}
				IESmartObjModel obj = (IESmartObjModel)model;
				Map<String, Boolean> visible = new HashMap<>(((OBJModel.OBJState)obj.getState()).getVisibilityMap());
				Tessellator tes = Tessellator.getInstance();
				BufferBuilder bb = tes.getBuffer();
				ItemCameraTransforms.TransformType transformType = obj.lastCameraTransform;
				List<Pair<BakedQuad, ShaderLayer>> quads = new ArrayList<>();
				for(String[] groups : callback.getSpecialGroups(stack, transformType, IESmartObjModel.tempEntityStatic))
				{
					GlStateManager.pushMatrix();
					Matrix4 mat = callback.getTransformForGroups(stack, groups, transformType, mc().player,
							ItemRendererIEOBJ.mat, partialTicks);
					GlStateManager.multMatrix(mat.toFloatBuffer(transform));
					boolean wasLightmapEnabled, wasLightingEnabled;
					{
						GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
						wasLightmapEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
						GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
						wasLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
					}
					boolean bright = callback.areGroupsFullbright(stack, groups);
					if(bright)
					{
						GlStateManager.disableLighting();
						ClientUtils.setLightmapDisabled(true);
					}
					renderQuadsForGroups(groups, callback, obj, quads, stack,
							sCase, shader, bb, tes, visible, partialTicks);
					if(bright)
					{
						if(wasLightingEnabled)
							GlStateManager.enableLighting();
						if(wasLightmapEnabled)
							ClientUtils.setLightmapDisabled(false);
					}
					GlStateManager.popMatrix();
				}
				renderQuadsForGroups(visible.keySet().toArray(new String[0]), callback, obj, quads, stack,
						sCase, shader, bb, tes, visible, partialTicks);
				GlStateManager.enableCull();
			}
		}
	}

	private void renderQuadsForGroups(String[] groups, IOBJModelCallback<ItemStack> callback, IESmartObjModel model,
									  List<Pair<BakedQuad, ShaderLayer>> quadsForGroup, ItemStack stack, ShaderCase sCase, ItemStack shader,
									  BufferBuilder bb, Tessellator tes, Map<String, Boolean> visible, float partialTicks)
	{
		quadsForGroup.clear();
		for(String g : groups)
		{
			if(visible.getOrDefault(g, Boolean.FALSE)&&callback.shouldRenderGroup(stack, g))
				quadsForGroup.addAll(model.addQuadsForGroup(callback, stack, g, sCase, shader)
						.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			visible.remove(g);
		}
		if(!callback.areGroupsFullbright(stack, groups))
			bb.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		else
			bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		VertexBufferConsumer vbc = new VertexBufferConsumer(bb);
		ShaderLayer lastShaderLayer = null;
		for(Pair<BakedQuad, ShaderLayer> pair : quadsForGroup)
		{
			BakedQuad bq = pair.getKey();
			ShaderLayer layer = pair.getValue();
			//Switch to or between dynamic layers
			boolean switchDynamic = layer!=lastShaderLayer;
			if(switchDynamic)
			{
				//interrupt batch
				tes.draw();

				if(lastShaderLayer!=null)//finish dynamic call on last layer
					lastShaderLayer.modifyRender(false, partialTicks);

				//set new layer
				lastShaderLayer = layer;

				if(lastShaderLayer!=null)//start dynamic call on layer
					lastShaderLayer.modifyRender(true, partialTicks);
				//start new batch
				if(!callback.areGroupsFullbright(stack, groups))
					bb.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
				else
					bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			}
			bq.pipe(vbc);
		}
		tes.draw();
		if(lastShaderLayer!=null)//finish dynamic call on final layer
			lastShaderLayer.modifyRender(false, partialTicks);

	}
}
