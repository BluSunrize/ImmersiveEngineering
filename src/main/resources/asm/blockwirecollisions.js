function initializeCoreMod() {
    return {
        'IE block update callback': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ServerWorld',
                'methodName': 'func_184138_a',
                'methodDesc': '(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V'
            },
            'transformer': function (method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var opcodes = Java.type('org.objectweb.asm.Opcodes');
                var callback = ASMAPI.listOf(
                    new VarInsnNode(opcodes.ALOAD, 0),
                    new VarInsnNode(opcodes.ALOAD, 1),
                    new VarInsnNode(opcodes.ALOAD, 2),
                    new VarInsnNode(opcodes.ALOAD, 3),
                    new VarInsnNode(opcodes.ILOAD, 4),
                    ASMAPI.buildMethodCall(
                        "blusunrize/immersiveengineering/common/wires/WireCollisions",
                        "notifyBlockUpdate",
                        "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;I)V",
                        ASMAPI.MethodType.STATIC
                    ));
                if (ASMAPI.insertInsnList(
                    method,
                    ASMAPI.MethodType.INTERFACE,
                    "java/util/Set",
                    "iterator",
                    "()Ljava/util/Iterator;",
                    callback,
                    ASMAPI.InsertMode.INSERT_BEFORE
                ) === false) {
                    throw "Failed to insert block update callback";
                }

                ASMAPI.log("INFO", "Inserted block update callback", {});
                return method;
            }
        }
    }
}