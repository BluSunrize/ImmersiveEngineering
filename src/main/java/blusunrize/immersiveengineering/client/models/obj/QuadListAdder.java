/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.QuadTransformer;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class QuadListAdder implements IModelBuilder<QuadListAdder>
{
	private final Consumer<BakedQuad> output;
	private final BiConsumer<BakedQuad, Direction> outputFace;
	private final Function<BakedQuad, BakedQuad> transform;

	public QuadListAdder(
			Consumer<BakedQuad> output,
			BiConsumer<BakedQuad, Direction> outputFace,
			TRSRTransformation transform
	)
	{
		this.output = output;
		this.outputFace = outputFace;
		this.transform = ApiUtils.transformQuad(transform, x -> x);
	}

	public QuadListAdder(Consumer<BakedQuad> output, TRSRTransformation transform)
	{
		this(output, (quad, dir) -> output.accept(quad), transform);
	}

	@Nonnull
	@Override
	public QuadListAdder addFaceQuad(@Nonnull Direction facing, @Nonnull BakedQuad quad)
	{
		outputFace.accept(transform.apply(quad), facing);
		return this;
	}

	@Nonnull
	@Override
	public QuadListAdder addGeneralQuad(@Nonnull BakedQuad quad)
	{
		output.accept(transform.apply(quad));
		return this;
	}

	@Nonnull
	@Override
	public IBakedModel build()
	{
		throw new UnsupportedOperationException();
	}
}
