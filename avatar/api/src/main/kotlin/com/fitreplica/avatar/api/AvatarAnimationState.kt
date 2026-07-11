package com.fitreplica.avatar.api

/**
 * Defined from day one, even though impl-2d only meaningfully supports IDLE/WALK —
 * this lets a future impl-3d add richer states additively, without a breaking
 * interface change to every AvatarRenderer call site.
 */
enum class AvatarAnimationState { IDLE, WALK, TURN, SHOWCASE }
