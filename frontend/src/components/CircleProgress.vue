<template>
  <div class="circle-progress" :style="{ width: size + 'px', height: size + 'px' }" :data-type="type">
    <svg :width="size" :height="size" :viewBox="viewBox">
      <!-- 背景轨道 -->
      <path
        :d="path"
        fill="none"
        :stroke="trackColor"
        :stroke-width="strokeWidth"
        stroke-linecap="round"
      />

      <!-- 进度 -->
      <path
        :d="path"
        fill="none"
        :stroke="color"
        :stroke-width="strokeWidth"
        stroke-linecap="round"
        :stroke-dasharray="pathLength"
        :stroke-dashoffset="dashOffset"
        class="progress-path"
      />
    </svg>

    <!-- 中间内容 -->
    <div class="content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  /** 进度 0-100 */
  percentage: {
    type: Number,
    default: 50,
  },
  /** 尺寸 */
  size: {
    type: Number,
    default: 200,
  },
  /** 线宽 */
  strokeWidth: {
    type: Number,
    default: 10,
  },
  /** 进度颜色 */
  color: {
    type: String,
    default: "#1677ff",
  },
  /** 背景颜色 */
  trackColor: {
    type: String,
    default: "#eee",
  },
  /** 类型: circle / arc */
  type: {
    type: String,
    default: "circle",
  },
});

const radius = computed(() => (props.size - props.strokeWidth) / 2);
const center = computed(() => props.size / 2);

/** 路径 */
const path = computed(() => {
  const r = radius.value;
  const c = center.value;

  if (props.type === "arc") {
    const angle = 240;
    const startAngle = 270 - angle / 2;  // 左下
    const endAngle = 270 + angle / 2;    // 右下

    const start = polarToCartesian(c, c, r, startAngle);
    const end = polarToCartesian(c, c, r, endAngle);

    return `
      M ${start.x} ${start.y}
      A ${r} ${r} 0 1 1 ${end.x} ${end.y}
    `;
  } else {
    // 全圆
    return `
      M ${c}, ${c}
      m -${r}, 0
      a ${r},${r} 0 1,1 ${2 * r},0
      a ${r},${r} 0 1,1 -${2 * r},0
    `;
  }
});
const polarToCartesian = (cx, cy, r, angle) => {
  const rad = (angle - 90) * Math.PI / 180.0;
  return {
    x: cx + (r * Math.cos(rad)),
    y: cy + (r * Math.sin(rad))
  };
}

/** 总长度 */
const pathLength = computed(() => {
  if (props.type === "arc") {
    return (4 / 3) * Math.PI * radius.value;
  } else {
    return 2 * Math.PI * radius.value;
  }
});

/** 偏移量（控制进度） */
const dashOffset = computed(() => {
  return pathLength.value * (1 - props.percentage / 100);
});

/** viewBox */
const viewBox = computed(() => {
  const half = props.strokeWidth / 2;
  return `0 0 ${props.size} ${props.size}`;
});
</script>

<style scoped>
.circle-progress {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

svg {
  transform: rotate(-90deg);
  overflow: visible;
}

/* 弧形不旋转 */
.circle-progress[data-type="arc"] svg {
  transform: rotate(90deg);
}

.progress-path {
  transition: stroke-dashoffset 0.6s ease;
}

/* 中间内容 */
.content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}
</style>