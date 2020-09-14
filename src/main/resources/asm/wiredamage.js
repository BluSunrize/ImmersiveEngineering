function initializeCoreMod() {
    return {
        'IE wire damage': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.entity.Entity',
                'methodName': 'func_145775_I',
                'methodDesc': '()V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
                var opcodes = Java.type('org.objectweb.asm.Opcodes');
                var callback = ASMAPI.listOf(
                    new VarInsnNode(opcodes.ALOAD, 4),
                    new VarInsnNode(opcodes.ALOAD, 0),
                    ASMAPI.buildMethodCall(
                        "blusunrize/immersiveengineering/common/wires/WireCollisions",
                        "handleEntityCollision",
                        "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V",
                        ASMAPI.MethodType.STATIC
                    ));
                var target = ASMAPI.mapMethod("func_191955_a")
                if (ASMAPI.insertInsnList(
                        method,
                        ASMAPI.MethodType.VIRTUAL,
                        "net/minecraft/entity/Entity",
                        target,
                        "(Lnet/minecraft/block/BlockState;)V",
                        callback,
                        ASMAPI.InsertMode.INSERT_BEFORE
                    ) === false) {
                    throw "Failed to insert entity-block collision callback";
                }

                ASMAPI.log("INFO", "Inserted wire collision callback", {});
                return method;
            }
        }
    }
}