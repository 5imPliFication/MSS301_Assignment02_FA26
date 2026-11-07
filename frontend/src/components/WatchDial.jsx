// Abstract dial illustration; deliberately not a product photograph.
export default function WatchDial({ variant = 0, large = false }) {
  const colors = ["#2d4840", "#25384a", "#c7b99c", "#303535", "#f0ece2"];
  const dial = colors[variant % colors.length];
  const light = variant % colors.length === 2 || variant % colors.length === 4;
  return (
    <div
      className={`watch-art ${large ? "watch-art-large" : ""}`}
      aria-hidden="true"
    >
      <svg viewBox="0 0 100 132" fill="none">
        <rect
          x="35"
          y="2"
          width="30"
          height="128"
          rx="10"
          fill={variant % 2 ? "#333b37" : "#a9aca6"}
        />
        {[13, 21, 29, 100, 108, 116].map((y) => (
          <path
            key={y}
            d={`M37 ${y}h26`}
            stroke={variant % 2 ? "#57605a" : "#dce0d8"}
            strokeWidth="2"
          />
        ))}
        <rect x="82" y="60" width="7" height="13" rx="2" fill="#999f96" />
        <circle cx="50" cy="66" r="37" fill="#8f978f" />
        <circle cx="50" cy="66" r="34" fill="#d7d9cf" />
        <circle cx="50" cy="66" r="30.5" fill={dial} />
        {Array.from({ length: 12 }, (_, i) => (
          <line
            key={i}
            x1="50"
            y1="39"
            x2="50"
            y2={i % 3 === 0 ? "44" : "42"}
            stroke={light ? "#4c514b" : "#dddcc7"}
            strokeWidth={i % 3 === 0 ? 2 : 1}
            transform={`rotate(${i * 30} 50 66)`}
          />
        ))}
        <path
          d="M50 66L38 53M50 66L65 49"
          stroke={light ? "#293a31" : "#f4f1df"}
          strokeWidth="2.5"
          strokeLinecap="round"
        />
        <path d="M50 66L59 83" stroke="#c19c5e" strokeWidth="1" />
        <circle cx="50" cy="66" r="2.5" fill="#c19c5e" />
        <path
          d="M43 79h14M46 82h8"
          stroke={light ? "#747b6a" : "#879589"}
          strokeWidth=".7"
        />
      </svg>
    </div>
  );
}
