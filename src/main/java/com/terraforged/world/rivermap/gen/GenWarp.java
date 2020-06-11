package com.terraforged.world.rivermap.gen;

import com.terraforged.n2d.domain.Domain;

public class GenWarp {

    public final Domain lake;
    public final Domain river;

    public GenWarp(int seed) {
        this.lake = Domain.warp(++seed, 200, 1, 300)
                .add(Domain.warp(++seed, 50, 2, 50));
        this.river = Domain.warp(++seed, 400, 1, 350)
                .add(Domain.warp(++seed, 80, 1, 35))
                .add(Domain.warp(++seed, 16, 1, 2));
    }
}
