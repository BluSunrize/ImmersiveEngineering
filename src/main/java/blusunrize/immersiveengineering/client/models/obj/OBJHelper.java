package blusunrize.immersiveengineering.client.models.obj;

import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.obj.MaterialLibrary2;
import net.minecraftforge.client.model.obj.OBJModel2;
import net.minecraftforge.client.model.obj.OBJModel2.ModelGroup;
import net.minecraftforge.client.model.obj.OBJModel2.ModelObject;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

//Helper functions to extract info from the new OBJModel2, which is far less permissive than ObjModel
public class OBJHelper
{
	private static Field OBJModel2_parts;
	private static Field OBJModel2_texCoords;
	private static Method OBJModel2_makeQuad;
	private static Field ModelGroup_parts;
	private static Field ModelObject_meshes;
	private static Class<?> ModelMesh;
	private static Field ModelMesh_faces;
	private static Field ModelMesh_mat;
	private static Method ModelMesh_isFullbright;

	static
	{
		try
		{
			OBJModel2_parts = OBJModel2.class.getDeclaredField("parts");
			OBJModel2_parts.setAccessible(true);
			OBJModel2_texCoords = OBJModel2.class.getDeclaredField("texCoords");
			OBJModel2_texCoords.setAccessible(true);
			OBJModel2_makeQuad = OBJModel2.class.getDeclaredMethod("makeQuad", int[][].class, int.class, Vector4f.class,
					Vector4f.class, boolean.class, TextureAtlasSprite.class, VertexFormat.class, Optional.class);
			OBJModel2_makeQuad.setAccessible(true);
			ModelGroup_parts = ModelGroup.class.getDeclaredField("parts");
			ModelGroup_parts.setAccessible(true);
			ModelObject_meshes = ModelObject.class.getDeclaredField("meshes");
			ModelObject_meshes.setAccessible(true);
			ModelMesh = Class.forName("net.minecraftforge.client.model.obj.OBJModel2$ModelMesh");
			ModelMesh_faces = ModelMesh.getDeclaredField("faces");
			ModelMesh_faces.setAccessible(true);
			ModelMesh_mat = ModelMesh.getDeclaredField("mat");
			ModelMesh_mat.setAccessible(true);
			ModelMesh_isFullbright = ModelMesh.getDeclaredMethod("isFullbright");
			ModelMesh_isFullbright.setAccessible(true);
		} catch(NoSuchFieldException|NoSuchMethodException|ClassNotFoundException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Incompatible Forge version!");
		}
	}

	public static Map<String, ModelGroup> getGroups(OBJModel2 model)
	{
		return get(OBJModel2_parts, model);
	}

	public static List<Vector2f> getTexCoords(OBJModel2 model)
	{
		return get(OBJModel2_texCoords, model);
	}

	public static Map<String, ModelObject> getParts(ModelGroup group)
	{
		return get(ModelGroup_parts, group);
	}

	public static List<ModelObject> getRecursiveParts(OBJModel2 model)
	{
		List<ModelObject> ret = new ArrayList<>();
		Deque<ModelGroup> toExtract = new ArrayDeque<>(getGroups(model).values());
		while(ret.size() < 1024&&!toExtract.isEmpty())
		{
			ModelGroup next = toExtract.pop();
			for(ModelObject object : getParts(next).values())
			{
				if(object instanceof ModelGroup)
					toExtract.push((ModelGroup)object);
				else
					ret.add(object);
			}
		}
		return ret;
	}

	public static List<MeshWrapper> getMeshes(ModelObject object)
	{
		List<?> meshes = get(ModelObject_meshes, object);
		List<MeshWrapper> ret = new ArrayList<>();
		for(Object o : meshes)
			ret.add(new MeshWrapper(o));
		return ret;
	}

	public static Pair<BakedQuad, Direction> makeQuad(OBJModel2 model, int[][] indices, int tintIndex, Vector4f colorTint,
													  Vector4f ambientColor, boolean isFullbright, TextureAtlasSprite texture,
													  VertexFormat format, Optional<TRSRTransformation> transform)
	{
		return invoke(OBJModel2_makeQuad, model, indices, tintIndex, colorTint, ambientColor, isFullbright, texture, format, transform);
	}

	@SuppressWarnings("unchecked")
	private static <T> T get(Field f, Object e)
	{
		try
		{
			return (T)f.get(e);
		} catch(IllegalAccessException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T invoke(Method m, Object e, Object... args)
	{
		try
		{
			return (T)m.invoke(e, args);
		} catch(IllegalAccessException|InvocationTargetException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static class MeshWrapper
	{
		private final Object mesh;

		private MeshWrapper(Object mesh)
		{
			Preconditions.checkArgument(ModelMesh.isInstance(mesh));
			this.mesh = mesh;
		}

		public List<int[][]> getFaces()
		{
			return get(ModelMesh_faces, mesh);
		}

		public MaterialLibrary2.Material getMaterial()
		{
			return get(ModelMesh_mat, mesh);
		}

		public boolean isFullbright()
		{
			return invoke(ModelMesh_isFullbright, mesh);
		}
	}
}
