using UnityEngine;
#if ENABLE_INPUT_SYSTEM
using UnityEngine.InputSystem;
#endif

// Works with both the old Input Manager and the new Input System package.
public static class GameInput
{
    public static bool JumpPressed()
    {
#if ENABLE_INPUT_SYSTEM
        Keyboard k = Keyboard.current;
        Mouse m = Mouse.current;
        Touchscreen t = Touchscreen.current;
        return (k != null && (k.spaceKey.wasPressedThisFrame || k.upArrowKey.wasPressedThisFrame))
            || (m != null && m.leftButton.wasPressedThisFrame)
            || (t != null && t.primaryTouch.press.wasPressedThisFrame);
#else
        return Input.GetKeyDown(KeyCode.Space)
            || Input.GetKeyDown(KeyCode.UpArrow)
            || Input.GetMouseButtonDown(0);
#endif
    }
}
