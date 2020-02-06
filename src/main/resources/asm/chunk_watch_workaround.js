//TODO remove this coremod once the Forge events are fixed
function createCallback(name) {
    var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
    var opcodes = Java.type('org.objectweb.asm.Opcodes');
    var worldField = ASMAPI.mapField("field_219255_i");
    return ASMAPI.listOf(
        new VarInsnNode(opcodes.ALOAD, 0),
        new FieldInsnNode(opcodes.GETFIELD, "net/minecraft/world/server/ChunkManager",
            worldField, "Lnet/minecraft/world/server/ServerWorld;"),
        new VarInsnNode(opcodes.ALOAD, 2),
        new VarInsnNode(opcodes.ALOAD, 1),
        ASMAPI.buildMethodCall(
            "blusunrize/immersiveengineering/common/wires/WireSyncManager",
            name,
            "(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/ChunkPos" +
            ";Lnet/minecraft/entity/player/ServerPlayerEntity;)V",
            ASMAPI.MethodType.STATIC
        ));
}

function initializeCoreMod() {
    return {
        'ChunkWatchEvent workaround': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.server.ChunkManager',
                'methodName': 'func_219199_a',
                'methodDesc': '(Lnet/minecraft/entity/player/ServerPlayerEntity;Lnet/minecraft/util/math/ChunkPos;[Lnet/minecraft/network/IPacket;ZZ)V'
            },
            'transformer': function (method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var unloadTarget = ASMAPI.mapMethod("func_213845_a");
                if (ASMAPI.insertInsnList(
                    method,
                    ASMAPI.MethodType.VIRTUAL,
                    "net/minecraft/entity/player/ServerPlayerEntity",
                    unloadTarget,
                    "(Lnet/minecraft/util/math/ChunkPos;)V",
                    createCallback("onChunkUnWatch"),
                    ASMAPI.InsertMode.INSERT_AFTER
                ) === false) {
                    throw "Failed to insert chunk unwatch callback";
                }
                var loadTarget = ASMAPI.mapMethod("func_219219_b");
                if (ASMAPI.insertInsnList(
                    method,
                    ASMAPI.MethodType.VIRTUAL,
                    "net/minecraft/world/server/ChunkManager",
                    loadTarget,
                    "(J)Lnet/minecraft/world/server/ChunkHolder;",
                    createCallback("onChunkWatch"),
                    ASMAPI.InsertMode.INSERT_AFTER
                ) === false) {
                    throw "Failed to insert chunk watch callback";
                }

                ASMAPI.log("INFO", "Inserted chunk (un)watch callbacks", {});
                return method;
            }
        }
    }
}