import { useEffect } from "react";

interface KeyboardNavOptions {
  onNext: () => void;
  onPrev: () => void;
  onFirst: () => void;
  onLast: () => void;
  onToggleFullscreen?: () => void;
  enabled?: boolean;
}

export function useKeyboardNav({
  onNext,
  onPrev,
  onFirst,
  onLast,
  onToggleFullscreen,
  enabled = true,
}: KeyboardNavOptions) {
  useEffect(() => {
    if (!enabled) return;

    const handler = (e: KeyboardEvent) => {
      if (e.target instanceof HTMLInputElement || e.target instanceof HTMLTextAreaElement) {
        return;
      }

      switch (e.key) {
        case "ArrowRight":
        case "d":
        case " ":
          e.preventDefault();
          onNext();
          break;
        case "ArrowLeft":
        case "a":
          e.preventDefault();
          onPrev();
          break;
        case "Home":
          e.preventDefault();
          onFirst();
          break;
        case "End":
          e.preventDefault();
          onLast();
          break;
        case "f":
          e.preventDefault();
          onToggleFullscreen?.();
          break;
      }
    };

    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [enabled, onNext, onPrev, onFirst, onLast, onToggleFullscreen]);
}
