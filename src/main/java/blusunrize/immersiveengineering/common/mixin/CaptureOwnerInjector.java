/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.mixin;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.invoke.InvokeInjector;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.injection.struct.InjectionNodes.InjectionNode;
import org.spongepowered.asm.mixin.injection.struct.Target;
import org.spongepowered.asm.mixin.injection.struct.Target.Extension;
import org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException;
import org.spongepowered.asm.util.Bytecode;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CaptureOwnerInjector extends InvokeInjector
{
	public CaptureOwnerInjector(InjectionInfo info)
	{
		super(info, "@CaptureOwner");
	}

	@Override
	protected void sanityCheck(Target target, List<InjectionPoint> injectionPoints)
	{
		super.sanityCheck(target, injectionPoints);
		if(this.methodArgs.length==0||this.methodArgs.length > 1+target.arguments.length)
			throw new InvalidInjectionException(
					this.info, "Target for CaptureOwner must take one arg (the owner to capture/modify) and then some prefix of the target argument"
			);
		if(!Objects.equals(this.returnType, this.methodArgs[0]))
			throw new InvalidInjectionException(
					this.info, "Target for CaptureOwner must return the type of the modified argument, not "+this.returnType
			);
		for(int i = 1; i < this.methodArgs.length; ++i)
			if(!Objects.equals(this.methodArgs[i], target.arguments[i-1]))
				throw new InvalidInjectionException(
						this.info, "Argument "+i+" must be (i-1)th arg of target: "+this.methodArgs[i]+" vs "+target.arguments[i-1]
				);
		// Do not check parameter type, this will be some superclass of the classes we get at the injection points and I
		// cba to figure out how to check that from here. Also, this is not API, it's internal use only, so you better
		// know what you're doing!
	}

	@Override
	protected void injectAtInvoke(Target target, InjectionNode node)
	{
		MethodInsnNode invokedNode = (MethodInsnNode)node.getCurrentTarget();
		Type[] realArgs = Type.getArgumentTypes(invokedNode.desc);
		// Top part of the stack before the method call
		Type[] argsWithThis = new Type[realArgs.length+1];
		argsWithThis[0] = Type.getType('L'+invokedNode.owner+';');
		System.arraycopy(realArgs, 0, argsWithThis, 1, realArgs.length);

		InsnList injected = new InsnList();
		Extension extraLocals = target.extendLocals();
		// Move method args&owner from stack into locals
		int[] storedArgs = storeArgs(target, argsWithThis, injected, 0);
		// Load owner of callback if not static
		if(!this.isStatic)
		{
			injected.add(new VarInsnNode(Opcodes.ALOAD, 0));
		}
		// Load owner to be modified
		pushArgs(argsWithThis, injected, storedArgs, 0, 1);
		// Load method arguments to be passed to the injector
		final int firstMethodArg = this.isStatic?0: 1;
		Bytecode.loadArgs(Arrays.copyOf(target.arguments, this.methodArgs.length-1), injected, firstMethodArg, -1);
		// Invoke, cast result since in RebuildTaskMixin we don't know the exact class
		invokeHandler(injected);
		injected.add(new TypeInsnNode(Opcodes.CHECKCAST, invokedNode.owner));
		// Restore stack state, replacing 0th arg by return value of callback
		pushArgs(argsWithThis, injected, storedArgs, 1, storedArgs.length);
		extraLocals.add(storedArgs[storedArgs.length-1]-target.getMaxLocals());
		target.insns.insertBefore(invokedNode, injected);
		target.extendStack().set(2-(extraLocals.get()-1)).apply();
		extraLocals.apply();
	}
}
