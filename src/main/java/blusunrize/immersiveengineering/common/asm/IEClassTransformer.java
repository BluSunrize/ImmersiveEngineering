/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.asm;

import com.google.common.collect.Maps;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author BluSunrize - 20.07.2017
 */
public class IEClassTransformer implements IClassTransformer
{
	private static Map<String, MethodTransformer[]> transformerMap = Maps.newHashMap();

	static
	{
		transformerMap.put("net.minecraft.client.model.ModelBiped", new MethodTransformer[]{
				new MethodTransformer("setRotationAngles", "func_78087_a", "(FFFFFFLnet/minecraft/entity/Entity;)V", methodNode ->
				{
					Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
					while(iterator.hasNext())
					{
						AbstractInsnNode anode = iterator.next();
						if(anode.getOpcode()==Opcodes.RETURN)
						{
							InsnList newInstructions = new InsnList();
							newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
							newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 7));
							newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "blusunrize/immersiveengineering/client/ClientUtils", "handleBipedRotations", "(Lnet/minecraft/client/model/ModelBiped;Lnet/minecraft/entity/Entity;)V", false));
							methodNode.instructions.insertBefore(anode, newInstructions);
						}
					}
				})
		});
		transformerMap.put("net.minecraft.entity.Entity", new MethodTransformer[]{
				new MethodTransformer("doBlockCollisions", "func_145775_I", "()V", (m) ->
				{
					//INVOKEVIRTUAL net/minecraft/block/Block.onEntityCollision (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V
					Iterator<AbstractInsnNode> iterator = m.instructions.iterator();
					while(iterator.hasNext())
					{
						AbstractInsnNode anode = iterator.next();
						if(anode.getOpcode()==Opcodes.INVOKEVIRTUAL)
						{
							MethodInsnNode n = (MethodInsnNode)anode;
							if(n.name.equals("onEntityCollision")||n.name.equals("func_180634_a"))
							{
								InsnList newInstructions = new InsnList();
								newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
								newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
								newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
										"blusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler", "handleEntityCollision",
										"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", false));
								m.instructions.insert(n, newInstructions);
							}
						}
					}
				})
		});
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass)
	{
		if(basicClass!=null&&transformerMap.containsKey(transformedName))
		{
			MethodTransformer[] transformers = transformerMap.get(transformedName);
			ClassReader reader = new ClassReader(basicClass);
			ClassNode node = new ClassNode();
			reader.accept(node, 0);

			for(MethodNode method : node.methods)
				for(MethodTransformer methodTransformer : transformers)
					if((methodTransformer.functionName.equals(method.name)||methodTransformer.srgName.equals(method.name))&&methodTransformer.functionDesc.equals(method.desc))
						methodTransformer.function.accept(method);

			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
			node.accept(writer);
			return writer.toByteArray();
		}
		return basicClass;
	}

	private static class MethodTransformer
	{
		final String functionName;
		final String srgName;
		final String functionDesc;
		final Consumer<MethodNode> function;

		private MethodTransformer(String funcName, String srgName, String funcDesc, Consumer<MethodNode> function)
		{
			this.functionName = funcName;
			this.srgName = srgName;
			this.functionDesc = funcDesc;
			this.function = function;
		}
	}
}
