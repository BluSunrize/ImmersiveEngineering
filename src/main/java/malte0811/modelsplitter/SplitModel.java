package malte0811.modelsplitter;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import malte0811.modelsplitter.math.EpsilonMath;
import malte0811.modelsplitter.math.EpsilonMath.Sign;
import malte0811.modelsplitter.math.Plane;
import malte0811.modelsplitter.math.Vec3d;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import malte0811.modelsplitter.model.Vertex;
import net.minecraft.util.math.Vec3i;

import java.util.Map;

public class SplitModel<Texture>
{
	private static final EpsilonMath EPS_MATH = new EpsilonMath(1e-5);

	private final Map<Vec3i, OBJModel<Texture>> submodels;

	public SplitModel(OBJModel<Texture> input)
	{
		ImmutableMap.Builder<Vec3i, OBJModel<Texture>> submodels = ImmutableMap.builder();
		for(Int2ObjectMap.Entry<OBJModel<Texture>> xSlice : splitInPlanes(input, 0).int2ObjectEntrySet())
		{
			Int2ObjectMap<OBJModel<Texture>> columns = splitInPlanes(xSlice.getValue(), 2);
			for(Int2ObjectMap.Entry<OBJModel<Texture>> zColumn : columns.int2ObjectEntrySet())
			{
				Int2ObjectMap<OBJModel<Texture>> dices = splitInPlanes(zColumn.getValue(), 1);
				for(Int2ObjectMap.Entry<OBJModel<Texture>> yDice : dices.int2ObjectEntrySet())
				{
					submodels.put(
							new Vec3i(xSlice.getIntKey(), yDice.getIntKey(), zColumn.getIntKey()),
							yDice.getValue()
					);
				}
			}
		}
		this.submodels = submodels.build();
	}

	public Map<Vec3i, OBJModel<Texture>> getParts()
	{
		return submodels;
	}

	private static <Texture> Int2ObjectMap<OBJModel<Texture>> splitInPlanes(OBJModel<Texture> input, int axis)
	{
		if(input.isEmpty())
		{
			return new Int2ObjectOpenHashMap<>();
		}
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(Polygon<Texture> f : input.getFaces())
		{
			for(Vertex v : f.getPoints())
			{
				double pos = v.getPosition().get(axis);
				min = Math.min(min, pos);
				max = Math.max(max, pos);
			}
		}
		final int firstBorder = EPS_MATH.ceil(min);
		final int lastBorder = EPS_MATH.floor(max);
		Int2ObjectMap<OBJModel<Texture>> modelPerSection = new Int2ObjectOpenHashMap<>();
		double[] vecData = new double[3];
		vecData[axis] = 1;
		final Vec3d normal = new Vec3d(vecData);
		for(int borderPos = firstBorder; borderPos <= lastBorder; ++borderPos)
		{
			Plane cut = new Plane(normal, borderPos);
			Map<EpsilonMath.Sign, OBJModel<Texture>> splitModel = input.split(cut);
			OBJModel<Texture> sectionModel = splitModel.get(EpsilonMath.Sign.NEGATIVE);
			putModel(modelPerSection, axis, borderPos-1, sectionModel);
			input = OBJModel.union(
					splitModel.get(EpsilonMath.Sign.POSITIVE),
					splitModel.get(Sign.ZERO)
			);
		}
		putModel(modelPerSection, axis, lastBorder, input);
		return modelPerSection;
	}

	private static <Texture> void putModel(
			Int2ObjectMap<OBJModel<Texture>> sectionModels,
			int axis,
			int section,
			OBJModel<Texture> baseSectionModel
	)
	{
		if(baseSectionModel!=null&&!baseSectionModel.isEmpty())
		{
			sectionModels.put(
					section,
					baseSectionModel.translate(axis, -section)
							.quadify()
			);
		}
	}
}
