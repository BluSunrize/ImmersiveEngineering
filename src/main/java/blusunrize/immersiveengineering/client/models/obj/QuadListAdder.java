/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.obj;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.IModelBuilder;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class QuadListAdder implements IModelBuilder<QuadListAdder>
{
	private final Consumer<BakedQuad> output;
	private final BiConsumer<BakedQuad, Direction> outputFace;

	public QuadListAdder(Consumer<BakedQuad> output, BiConsumer<BakedQuad, Direction> outputFace)
	{
		this.output = output;
		this.outputFace = outputFace;
	}
	
	public QuadListAdder(Consumer<BakedQuad> output) {
		this(output, (quad, dir) -> output.accept(quad));
	}

	@Nonnull
	@Override
	public QuadListAdder addFaceQuad(@Nonnull Direction facing, @Nonnull BakedQuad quad)
	{
		outputFace.accept(quad, facing);
		return this;
	}

	@Nonnull
	@Override
	public QuadListAdder addGeneralQuad(@Nonnull BakedQuad quad)
	{
		output.accept(quad);
		return this;
	}

	@Nonnull
	@Override
	public IBakedModel build()
	{
		throw new UnsupportedOperationException();
	}
}
