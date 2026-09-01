package aiefu.ebd;

import aiefu.ebd.network.S2CStringToClipboardPayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;
import java.util.Optional;

public class EBDCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context){
        for (String rootLiteral : new String[]{"ebd", "enchantbydoing", "eso"}) {
            dispatcher.register(Commands.literal(rootLiteral).requires(stack -> stack.hasPermission(4)).then(Commands.literal("learn")
                    .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("enchantment-id", StringArgumentType.greedyString())
                                    .executes(ctx -> learnEnchantmentById(ctx, EntityArgument.getPlayer(ctx,"player"), StringArgumentType.getString(ctx,"enchantment-id")))))));
            dispatcher.register(Commands.literal(rootLiteral).requires(stack -> stack.hasPermission(4)).then(Commands.literal("forget")
                    .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("enchantment-id", StringArgumentType.greedyString())
                                    .executes(ctx -> forgetEnchantment(ctx, EntityArgument.getPlayer(ctx,"player"), StringArgumentType.getString(ctx,"enchantment-id")))))));
            dispatcher.register(Commands.literal(rootLiteral).requires(stack -> stack.hasPermission(4)).then(Commands.literal("get-mat-id-in-hand")
                    .executes(EBDCommands::getMaterialId)));
            dispatcher.register(Commands.literal(rootLiteral).requires(stack -> stack.hasPermission(4)).then(Commands.literal("get-item-id-in-hand")
                    .executes(EBDCommands::getItemId)));
            dispatcher.register(Commands.literal(rootLiteral).requires(stack -> stack.hasPermission(4)).then(Commands.literal("get-enchantment-id-in-hand")
                    .executes(EBDCommands::getEnchantmentId)));
            dispatcher.register(Commands.literal(rootLiteral).requires(stack -> stack.hasPermission(4)).then(Commands.literal("learn-leveled")
                    .then(Commands.argument("player", EntityArgument.player()).then(Commands.argument("enchantment-id", StringArgumentType.greedyString())
                            .then(Commands.argument("operation", StringArgumentType.string()).then(Commands.argument("level", IntegerArgumentType.integer(1)).executes(ctx ->
                                    setLeveledEnchantment(ctx, EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "enchantment-id"),
                                            StringArgumentType.getString(ctx, "operation"), IntegerArgumentType.getInteger(ctx, "level")))))))));
            dispatcher.register(Commands.literal(rootLiteral).requires(stack -> stack.hasPermission(4))
                    .then(Commands.literal("skill")
                            .then(Commands.argument("player", EntityArgument.player())
                                    .then(Commands.argument("skill-id", StringArgumentType.string())
                                            .suggests((sCtx, builder) -> {
                                                for (SkillType type : SkillType.values()) {
                                                    builder.suggest(type.id);
                                                }
                                                return builder.buildFuture();
                                            })
                                            .then(Commands.literal("set")
                                                    .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                                                            .executes(ctx -> setSkillLevel(ctx, EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "skill-id"), IntegerArgumentType.getInteger(ctx, "level")))))
                                            .then(Commands.literal("add")
                                                    .then(Commands.argument("level", IntegerArgumentType.integer(-100, 100))
                                                            .executes(ctx -> addSkillLevel(ctx, EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "skill-id"), IntegerArgumentType.getInteger(ctx, "level")))))
                                            .then(Commands.literal("get")
                                                    .executes(ctx -> getSkillLevelCmd(ctx, EntityArgument.getPlayer(ctx, "player"), StringArgumentType.getString(ctx, "skill-id"))))))));
        }
    }

    public static int setLeveledEnchantment(CommandContext<CommandSourceStack> ctx, ServerPlayer targetPlayer, String enchantmentId, String operation, int level){
        ResourceLocation loc = ResourceLocation.parse(enchantmentId);
        Registry<Enchantment> registry = targetPlayer.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> holderOpt = registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, loc));
        if(holderOpt.isPresent()){
            Holder<Enchantment> enchantment = holderOpt.get();
            MutableComponent c = enchantment.value().description().copy();
            Object2IntOpenHashMap<ResourceLocation> learnedEnchantments = ((IServerPlayerAcc)targetPlayer).enchantment_overhaul$getUnlockedEnchantments();
            int maxLevel = EBDCommon.getMaximumPossibleEnchantmentLevel(enchantment);
            int i = learnedEnchantments.getInt(loc);
            switch (operation){
                case "add" -> {
                    int r = Math.min(maxLevel, i + level);
                    learnedEnchantments.put(loc, r);
                    ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.successfullyadded", c, r, targetPlayer.getDisplayName()), true);
                    targetPlayer.sendSystemMessage(Component.translatable("eso.youlearned", getFormattedNameLeveled(enchantment, r)).withStyle(ChatFormatting.GOLD), true);
                }
                case "set" -> {
                    int r = Math.min(level, maxLevel);
                    if(r == i){
                        ctx.getSource().sendFailure(Component.translatable("eso.command.feedback.playerknows", targetPlayer.getDisplayName(), c));
                    } else {
                        learnedEnchantments.put(loc, Math.min(level, maxLevel));
                        ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.successfullyadded", c, r, targetPlayer.getDisplayName()), true);
                        targetPlayer.sendSystemMessage(Component.translatable("eso.youlearned", getFormattedNameLeveled(enchantment, r)).withStyle(ChatFormatting.GOLD), true);
                    }
                }
                case "sub", "subtract" -> {
                    int r = i - level;
                    if(r < 1){
                        learnedEnchantments.removeInt(loc);
                        targetPlayer.sendSystemMessage(Component.translatable("eso.youforgot", c), true);
                        ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.removedEnchantment", c, targetPlayer.getDisplayName()), true);
                    } else {
                        learnedEnchantments.put(loc, r);
                        ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.successfullyadded", c, r, targetPlayer.getDisplayName()), true);
                        targetPlayer.sendSystemMessage(Component.translatable("eso.youlearned", getFormattedNameLeveled(enchantment, r)).withStyle(ChatFormatting.GOLD), true);
                    }
                }
                default -> ctx.getSource().sendFailure(Component.translatable("eso.command.feedback.invalidoperation"));
            }
        } else ctx.getSource().sendFailure(Component.translatable("eso.command.encantmentnotfound", enchantmentId));
        return 0;
    }

    public static MutableComponent getFormattedNameLeveled(Holder<Enchantment> e, int l){
        MutableComponent msg = Component.literal("[").withStyle(ChatFormatting.DARK_PURPLE);
        msg.append(Enchantment.getFullname(e, l));
        msg.append(Component.literal("]"));
        return msg;
    }

    public static int getMaterialId(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        String mat;
        if(item instanceof TieredItem ti){
            Tier t = ti.getTier();
            if(t instanceof Enum<?> e){
                mat = e.name();
            } else mat = t.getClass().getSimpleName();
        } else if(item instanceof ArmorItem ai){
            mat = ai.getMaterial().unwrapKey().map(key -> key.location().getPath()).orElse("");
        } else {
            mat = "null";
        }
        ctx.getSource().sendSuccess(() -> Component.literal(mat), true);
        copyToClipboard(player, mat);
        return 0;
    }

    public static int getItemId(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Item item = player.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(item);
        ctx.getSource().sendSuccess(() -> Component.literal(loc.toString()), true);
        copyToClipboard(player, loc.toString());
        return 0;
    }

    public static int getEnchantmentId(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(!stack.getEnchantments().isEmpty()){
            net.minecraft.world.item.enchantment.ItemEnchantments enchs = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            StringBuilder enchantments = new StringBuilder();
            for (Map.Entry<Holder<Enchantment>, Integer> e : enchs.entrySet()) {
                ResourceLocation loc = e.getKey().unwrapKey().map(ResourceKey::location).orElse(null);
                if(loc != null){
                    String s = loc.toString();
                    ctx.getSource().sendSuccess(() -> Component.literal(s), true);
                    enchantments.append(s).append(" ");
                }
            }
            String s = enchantments.toString();
            if (s.length() > 0) {
                copyToClipboard(player, s.substring(0, s.length() - 1));
            }
        }
        return 0;
    }

    public static void copyToClipboard(ServerPlayer player, String s){
        PacketDistributor.sendToPlayer(player, new S2CStringToClipboardPayload(s));
    }

    public static int forgetEnchantment(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String id){
        if(id.equalsIgnoreCase("all")){
            revokeAll(ctx, player);
        } else {
            ResourceLocation loc = ResourceLocation.parse(id);
            Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Optional<Holder.Reference<Enchantment>> holderOpt = registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, loc));
            if(holderOpt.isPresent() && player instanceof IServerPlayerAcc acc){
                Holder<Enchantment> enchantment = holderOpt.get();
                MutableComponent discId = enchantment.value().description().copy();
                MutableComponent c = Component.literal("[").withStyle(ChatFormatting.DARK_PURPLE);
                c.append(discId);
                c.append(Component.literal("]"));
                if(acc.enchantment_overhaul$getUnlockedEnchantments().removeInt(loc) != 0){
                    player.sendSystemMessage(Component.translatable("eso.youforgot", c), true);
                    ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.removedEnchantment", discId, player.getDisplayName()), true);
                } else ctx.getSource().sendFailure(Component.translatable("eso.command.feedback.doesnotknow", player.getDisplayName(), discId));
            } else ctx.getSource().sendFailure(Component.translatable("eso.command.encantmentnotfound", id));
        }
        return 0;
    }

    public static int learnEnchantmentById(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String id){
        if(id.equalsIgnoreCase("all")){
            grantAll(ctx, player);
        } else {
            ResourceLocation loc = ResourceLocation.parse(id);
            Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            Optional<Holder.Reference<Enchantment>> holderOpt = registry.getHolder(ResourceKey.create(Registries.ENCHANTMENT, loc));
            if(holderOpt.isPresent() && player instanceof IServerPlayerAcc acc){
                Holder<Enchantment> enchantment = holderOpt.get();
                MutableComponent discId = enchantment.value().description().copy();
                MutableComponent c = Component.literal("[").withStyle(ChatFormatting.DARK_PURPLE);
                c.append(discId);
                c.append(Component.literal("]"));
                if(acc.enchantment_overhaul$getUnlockedEnchantments().put(loc, EBDCommon.getMaximumPossibleEnchantmentLevel(enchantment)) < 1) {
                    player.sendSystemMessage(Component.translatable("eso.youlearned", c).withStyle(ChatFormatting.GOLD), true);
                    ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.addedEnchantment", discId, player.getDisplayName()).withStyle(ChatFormatting.GOLD), true);
                } else {
                    ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.playerknows", player.getDisplayName(), discId).withStyle(ChatFormatting.DARK_GREEN), true);
                }
            } else ctx.getSource().sendFailure(Component.translatable("eso.command.encantmentnotfound", id));
        }
        return 0;
    }

    public static void grantAll(CommandContext<CommandSourceStack> ctx, ServerPlayer player){
        if(player instanceof IServerPlayerAcc acc){
            Object2IntOpenHashMap<ResourceLocation> enchantments = acc.enchantment_overhaul$getUnlockedEnchantments();
            Registry<Enchantment> registry = player.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            for (Holder.Reference<Enchantment> e : registry.holders().toList()){
                ResourceLocation loc = e.key().location();
                enchantments.put(loc, EBDCommon.getMaximumPossibleEnchantmentLevel(e));
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.grantall", player.getDisplayName()), true);
            player.sendSystemMessage(Component.translatable("eso.command.allknowledge").withStyle(ChatFormatting.GOLD));
        }
    }

    public static void revokeAll(CommandContext<CommandSourceStack> ctx, ServerPlayer player){
        if(player instanceof IServerPlayerAcc acc){
            acc.enchantment_overhaul$getUnlockedEnchantments().clear();
            ctx.getSource().sendSuccess(() -> Component.translatable("eso.command.feedback.revokeall", player.getDisplayName()), true);
            player.sendSystemMessage(Component.translatable("eso.command.lostallknowledge").withStyle(ChatFormatting.GOLD));
        }
    }

    public static int setSkillLevel(CommandContext<CommandSourceStack> ctx, ServerPlayer targetPlayer, String skillId, int level) {
        SkillType type = SkillType.fromId(skillId);
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Skill '" + skillId + "' not found."));
            return 0;
        }
        IServerPlayerAcc acc = (IServerPlayerAcc) targetPlayer;
        acc.ebd$setSkillLevel(type.id, level);
        acc.ebd$setSkillXP(type.id, 0.0);
        
        EBDCommon.syncPlayerSkills(targetPlayer);
        
        ctx.getSource().sendSuccess(() -> Component.literal("Set level of " + type.displayName + " for " + targetPlayer.getScoreboardName() + " to " + level), true);
        return 1;
    }

    public static int addSkillLevel(CommandContext<CommandSourceStack> ctx, ServerPlayer targetPlayer, String skillId, int levelsToAdd) {
        SkillType type = SkillType.fromId(skillId);
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Skill '" + skillId + "' not found."));
            return 0;
        }
        IServerPlayerAcc acc = (IServerPlayerAcc) targetPlayer;
        int currentLevel = acc.ebd$getSkillLevel(type.id);
        int newLevel = Math.max(1, currentLevel + levelsToAdd);
        acc.ebd$setSkillLevel(type.id, newLevel);
        acc.ebd$setSkillXP(type.id, 0.0);
        
        EBDCommon.syncPlayerSkills(targetPlayer);
        
        ctx.getSource().sendSuccess(() -> Component.literal("Added " + levelsToAdd + " levels to " + type.displayName + " for " + targetPlayer.getScoreboardName() + " (now " + newLevel + ")"), true);
        return 1;
    }

    public static int getSkillLevelCmd(CommandContext<CommandSourceStack> ctx, ServerPlayer targetPlayer, String skillId) {
        SkillType type = SkillType.fromId(skillId);
        if (type == null) {
            ctx.getSource().sendFailure(Component.literal("Skill '" + skillId + "' not found."));
            return 0;
        }
        IServerPlayerAcc acc = (IServerPlayerAcc) targetPlayer;
        int level = acc.ebd$getSkillLevel(type.id);
        double xp = acc.ebd$getSkillXP(type.id);
        double needed = LBDConfig.INSTANCE.getXPNeededForLevel(type.id, level);
        
        ctx.getSource().sendSuccess(() -> Component.literal(targetPlayer.getScoreboardName() + "'s " + type.displayName + " level is: " + level + " (XP: " + String.format(java.util.Locale.US, "%.1f", xp) + "/" + String.format(java.util.Locale.US, "%.1f", needed) + ")"), true);
        return 1;
    }
}
