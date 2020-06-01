package com.terraforged.world.terrain;

public enum TerrainType implements ITerrain {
    NONE,
    DEEP_OCEAN {
        @Override
        public boolean isDeepOcean() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }
    },
    SHALLOW_OCEAN {
        @Override
        public boolean isShallowOcean() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }
    },
    COAST {
        @Override
        public boolean isCoast() {
            return true;
        }

        @Override
        public boolean isOverground() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }
    },
    BEACH {
        @Override
        public boolean isCoast() {
            return true;
        }

        @Override
        public boolean isOverground() {
            return true;
        }

        @Override
        public boolean overridesRiver() {
            return true;
        }
    },
    RIVER {
        @Override
        public boolean isRiver() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }
    },
    LAKE {
        @Override
        public boolean isLake() {
            return true;
        }

        @Override
        public boolean isSubmerged() {
            return true;
        }
    },
    WETLAND {
        @Override
        public boolean isWetland() {
            return true;
        }
    },
    FLATLAND {
        @Override
        public boolean isFlat() {
            return true;
        }

        @Override
        public boolean isOverground() {
            return true;
        }
    },
    LOWLAND {
        @Override
        public boolean isOverground() {
            return true;
        }
    },
    HIGHLAND {
        @Override
        public boolean isOverground() {
            return true;
        }
    },
    ;



    public TerrainType getDominant(TerrainType other) {
        if (ordinal() > other.ordinal()) {
            return this;
        }
        return other;
    }
}
