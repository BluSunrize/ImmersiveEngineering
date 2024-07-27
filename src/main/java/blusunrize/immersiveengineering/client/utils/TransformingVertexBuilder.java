/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.utils.Color4;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

public final class TransformingVertexBuilder extends VertexConsumerWrapper
{
	private final PoseStack transform;
	private Vec2 globalUv = null;
	private Vec2i globalOverlay = null;
	private Vec2i globalLightmap = null;
	private Vector3f globalNormal = null;
	private Color4 globalColor = null;

	public TransformingVertexBuilder(VertexConsumer base, PoseStack transform)
	{
		super(base);
		this.transform = transform;
	}

	public TransformingVertexBuilder(VertexConsumer base)
	{
		this(base, new PoseStack());
	}

	public TransformingVertexBuilder(MultiBufferSource buffer, RenderType type, PoseStack transform)
	{
		this(buffer.getBuffer(type), transform);
	}

	public TransformingVertexBuilder(MultiBufferSource buffer, RenderType type)
	{
		this(buffer, type, new PoseStack());
	}

	@Nonnull
	@Override
	public VertexConsumer addVertex(float x, float y, float z)
	{
		var pos = new Vector4f(x, y, z, 1);
		pos.mul(this.transform.last().pose());
		super.addVertex(pos.x, pos.y, pos.z);
		if(globalUv!=null)
			setUv(globalUv.x, globalUv.y);
		if(globalOverlay!=null)
			setUv1(globalOverlay.x, globalOverlay.y);
		if(globalLightmap!=null)
			setUv2(globalLightmap.x, globalLightmap.y);
		if(globalNormal!=null)
			setNormal(globalNormal.x, globalNormal.y, globalNormal.z);
		if(globalColor!=null)
			setColor(globalColor.r(), globalColor.g(), globalColor.b(), globalColor.a());
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setNormal(float x, float y, float z)
	{
		var normal = new Vector3f(x, y, z);
		normal.mul(transform.last().normal());
		return super.setNormal(normal.x, normal.y, normal.z);
	}

	public void defaultColor(float r, float g, float b, float a)
	{
		globalColor = new Color4(r, g, b, a);
	}

	public void unsetDefaultColor()
	{
		globalColor = null;
	}

	public void setUV(Vec2 uv)
	{
		globalUv = uv;
	}

	public void setDefaultLight(int light)
	{
		globalLightmap = new Vec2i(light&255, light>>16);
	}

	public void setDefaultNormal(float x, float y, float z)
	{
		Vector3f vec = new Vector3f(x, y, z);
		vec.normalize();
		globalNormal = vec;
	}

	public void setDefaultOverlay(int packedOverlayIn)
	{
		globalOverlay = new Vec2i(packedOverlayIn&0xffff, packedOverlayIn>>16);
	}

	private record Vec2i(int x, int y)
	{
	}
}
