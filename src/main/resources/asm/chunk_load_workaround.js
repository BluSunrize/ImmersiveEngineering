/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

//TODO remove this coremod once Forge PR 6610 is merged
function createCallback() {
    var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var opcodes = Java.type('org.objectweb.asm.Opcodes');
    return ASMAPI.listOf(
        new VarInsnNode(opcodes.ALOAD, 24),//ichunk
        new VarInsnNode(opcodes.ALOAD, 4),//NBT
        ASMAPI.buildMethodCall(
            "blusunrize/immersiveengineering/common/world/IEWorldGen",
            "chunkLoad",
            "(Lnet/minecraft/world/chunk/IChunk;Lnet/minecraft/nbt/CompoundNBT;)V",
            ASMAPI.MethodType.STATIC
        ));
}

function initializeCoreMod() {
    return {
        'ChunkDataEvent.Load workaround': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.chunk.storage.ChunkSerializer',
                'methodName': 'func_222656_a',
                'methodDesc': '(Lnet/minecraft/world/server/ServerWorld;' +
                    'Lnet/minecraft/world/gen/feature/template/TemplateManager;' +
                    'Lnet/minecraft/village/PointOfInterestManager;' +
                    'Lnet/minecraft/util/math/ChunkPos;' +
                    'Lnet/minecraft/nbt/CompoundNBT;' +
                    ')Lnet/minecraft/world/chunk/ChunkPrimer;'
            },
            'transformer': function (method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                ASMAPI.log("INFO", "Entered transformer");
                var opcodes = Java.type('org.objectweb.asm.Opcodes');
                var targets = [];
                for (var i = 0; i < method.instructions.size(); ++i) {
                    var insn = method.instructions.get(i);
                    if (insn.getOpcode() === opcodes.ARETURN) {
                        targets[targets.length] = insn;
                    }
                }
                if (targets.length !== 2) {
                    ASMAPI.log("ERROR", "Expected to find exactly 2 targets for chunk load callback");
                    return;
                }
                method.instructions.insertBefore(targets[0], createCallback());
                method.instructions.insertBefore(targets[1], createCallback());

                ASMAPI.log("INFO", "Inserted chunk load callbacks", {});
                return method;
            }
        }
    }
}
