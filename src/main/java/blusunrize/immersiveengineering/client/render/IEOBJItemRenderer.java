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
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.models.obj.OBJHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.VertexBufferConsumer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class IEOBJItemRenderer extends ItemStackTileEntityRenderer
{
	public static final ItemStackTileEntityRenderer INSTANCE = new IEOBJItemRenderer();
	private static FloatBuffer transform = GLAllocation.createDirectFloatBuffer(16);

	@Override
	public void renderByItem(ItemStack stack)
	{
		GlStateManager.enableCull();
		float partialTicks = mc().getRenderPartialTicks();
		if(stack.getItem() instanceof IOBJModelCallback)
		{
			IOBJModelCallback<ItemStack> callback = (IOBJModelCallback<ItemStack>)stack.getItem();
			World w = IESmartObjModel.tempEntityStatic!=null?IESmartObjModel.tempEntityStatic.world: null;
			IBakedModel model = mc().getItemRenderer().getItemModelWithOverrides(stack, w,
					IESmartObjModel.tempEntityStatic);
			if(model instanceof IESmartObjModel)
			{
				GlStateManager.disableCull();

				ItemStack shader;
				ShaderCase sCase;
				{
					Pair<ItemStack, ShaderCase> tmp = stack.getCapability(CapabilityShader.SHADER_CAPABILITY)
							.map(wrapper ->
							{
								ItemStack shaderInner = wrapper.getShaderItem();
								ShaderCase sCaseInner = null;
								if(!shaderInner.isEmpty()&&shaderInner.getItem() instanceof IShaderItem)
									sCaseInner = ((IShaderItem)shaderInner.getItem()).getShaderCase(shaderInner, stack, wrapper.getShaderType());
								return new ImmutablePair<>(shaderInner, sCaseInner);
							})
							.orElse(new ImmutablePair<>(ItemStack.EMPTY, null));
					shader = tmp.getLeft();
					sCase = tmp.getRight();
				}
				IESmartObjModel obj = (IESmartObjModel)model;
				Set<String> visible = new HashSet<>();
				for(String g : OBJHelper.getGroups(obj.baseModel).keySet())
					if(callback.shouldRenderGroup(stack, g))
						visible.add(g);
				Tessellator tes = Tessellator.getInstance();
				BufferBuilder bb = tes.getBuffer();
				TransformType transformType = obj.lastCameraTransform;
				List<BakedQuad> quads = new ArrayList<>();
				for(String[] groups : callback.getSpecialGroups(stack, transformType, IESmartObjModel.tempEntityStatic))
				{
					GlStateManager.pushMatrix();
					Matrix4 mat = new Matrix4(callback.getTransformForGroups(stack, groups, transformType, mc().player,
							partialTicks).getMatrixVec());
					GlStateManager.multMatrix(mat.toFloatBuffer(transform));
					boolean wasLightmapEnabled, wasLightingEnabled;
					{
						GlStateManager.activeTexture(GLX.GL_TEXTURE1);
						wasLightmapEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
						GlStateManager.activeTexture(GLX.GL_TEXTURE0);
						wasLightingEnabled = GL11.glIsEnabled(GL11.GL_LIGHTING);
					}
					boolean bright = callback.areGroupsFullbright(stack, groups);
					if(bright)
					{
						GlStateManager.disableLighting();
						ClientUtils.setLightmapDisabled(true);
					}
					renderQuadsForGroups(groups, callback, obj, quads, stack,
							sCase, shader, true, bb, tes, visible, partialTicks);
					if(bright)
					{
						if(wasLightingEnabled)
							GlStateManager.enableLighting();
						if(wasLightmapEnabled)
							ClientUtils.setLightmapDisabled(false);
					}
					GlStateManager.popMatrix();
				}
				renderQuadsForGroups(visible.toArray(new String[0]), callback, obj, quads, stack,
						sCase, shader, false, bb, tes, visible, partialTicks);
				GlStateManager.enableCull();
			}
		}
	}

	private void renderQuadsForGroups(String[] groups, IOBJModelCallback<ItemStack> callback, IESmartObjModel model,
									  List<BakedQuad> quadsForGroup, ItemStack stack, ShaderCase sCase, ItemStack shader,
									  boolean dynamic, BufferBuilder bb, Tessellator tes, Set<String> visible,
									  float partialTicks)
	{
		quadsForGroup.clear();
		for(String g : groups)
		{
			if(visible.contains(g)&&callback.shouldRenderGroup(stack, g))
				quadsForGroup.addAll(model.addQuadsForGroup(callback, stack, g, sCase, !dynamic)
						.stream().filter(Objects::nonNull).collect(Collectors.toList()));
			visible.remove(g);
		}
		if(!callback.areGroupsFullbright(stack, groups))
			bb.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
		else
			bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
		VertexBufferConsumer vbc = new VertexBufferConsumer(bb);
		ShaderLayer lastShaderLayer = null;
		for(BakedQuad bq : quadsForGroup)
		{
			//Switch to or between dynamic layers
			/*TODO
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
			 */
			bq.pipe(vbc);
		}
		tes.draw();
		if(lastShaderLayer!=null)//finish dynamic call on final layer
			lastShaderLayer.modifyRender(false, partialTicks);

	}
}
