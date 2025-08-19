# AzaharPlus External Display Casting Implementation

## 🎯 Overview
This implementation adds external display support for Nintendo 3DS dual-screen emulation, rendering the **TOP 3DS screen on an external display** while keeping the **BOTTOM 3DS screen with touch input on the phone**.

## 🏗️ Architecture

### Package Structure
```
com.azaharplus.cast/
├── CastingManager.kt           # Main orchestrator
├── adapters/
│   ├── CastingAdapter.kt       # Interface for casting strategies
│   ├── PresentationAdapter.kt  # Wired/DeX displays (primary)
│   └── ProjectionAdapter.kt    # MediaProjection fallback
└── surface/
    └── TopScreenSurface.kt     # Surface management for 3DS top screen
```

### Strategy Pattern
1. **Primary**: `PresentationAdapter` for wired/DeX displays (auto-detect)
2. **Fallback**: `ProjectionAdapter` for wireless casting (user-initiated)

### Integration Points
- **EmulationActivity**: Lifecycle management, casting toggle
- **InputOverlay**: Touch input filtering when casting active
- **NativeLibrary**: JNI bridge for surface management
- **native.cpp**: Native surface handling (connects to existing RendererBase::secondary_window)

## 🔧 Implementation Status

### ✅ Completed (Scaffolding)
- [x] **CastingManager** - Unified casting orchestrator with lifecycle management
- [x] **PresentationAdapter** - DisplayManager + Presentation for wired displays
- [x] **ProjectionAdapter** - MediaProjection + VirtualDisplay for wireless
- [x] **TopScreenSurface** - Surface abstraction with 3DS aspect ratio handling
- [x] **EmulationActivity integration** - Casting manager initialization and controls
- [x] **InputOverlay touch filtering** - Basic framework for casting-aware input
- [x] **JNI bridge methods** - setCastingSurface, removeCastingSurface, isCastingActive
- [x] **Native stub implementations** - Placeholder JNI handlers in native.cpp

### 🔄 TODO (Next Phase)
- [ ] **Connect to RendererBase::secondary_window** - Wire native casting surface to existing dual-window renderer
- [ ] **Bottom screen layout adjustment** - Modify layout when casting to use full primary display
- [ ] **Touch area bounds calculation** - Get actual bottom screen coordinates for precise input filtering
- [ ] **Settings UI integration** - Add casting preferences and controls
- [ ] **MediaProjection permission flow** - Complete ProjectionAdapter implementation
- [ ] **Aspect ratio optimization** - Proper 3DS top screen (400x240) scaling on external displays
- [ ] **Error handling and recovery** - Graceful handling of display disconnect/connect events
- [ ] **Performance optimization** - Minimize rendering overhead and ensure smooth dual-display operation

## 🎮 Usage Flow

### Automatic (Wired/DeX)
1. Connect external display (HDMI, USB-C, DeX)
2. CastingManager auto-detects via DisplayManager
3. Creates Presentation with SurfaceView for 3DS top screen
4. Native renderer receives secondary surface
5. Touch input automatically filtered to bottom screen area

### Manual (Wireless)
1. User enables casting in settings/menu
2. System requests MediaProjection permission
3. Creates VirtualDisplay for casting apps (Chromecast, etc.)
4. Top screen rendered to virtual display surface

## 📱 Touch Input Handling

When casting is **active**:
- Touch input filtered through `InputOverlay.shouldAllowTouchInput()`
- Only bottom screen area touches forwarded to native layer
- Virtual controls (overlay buttons) work normally
- Top screen becomes touch-read-only on external display

When casting is **inactive**:
- Normal dual-screen touch behavior
- All screen areas accept touch input

## 🔗 Native Integration

### Key JNI Methods
```kotlin
// Surface management
external fun setCastingSurface(surface: Surface): Boolean
external fun removeCastingSurface()
external fun isCastingActive(): Boolean
```

### Native Flow
```cpp
// Current implementation (stubbed)
ANativeWindow* casting_window = nullptr;
std::unique_ptr<EmuWindow_Android> secondary_window = nullptr;

// TODO: Connect to RendererBase constructor:
RendererBase(Core::System& system, Frontend::EmuWindow& window, 
             Frontend::EmuWindow* secondary_window)  // ← Use this!
```

## 🎯 3DS Screen Mapping

### Default Mode (Single Display)
```
┌─────────────────────┐
│                     │
│    TOP SCREEN       │  ← 400x240
│    (Touch: No)      │
│                     │
├─────────────────────┤
│                     │
│   BOTTOM SCREEN     │  ← 320x240  
│   (Touch: Yes)      │
│                     │
└─────────────────────┘
```

### Casting Mode (Dual Display)
```
📱 Phone Display          🖥️ External Display
┌─────────────────────┐   ┌─────────────────────┐
│                     │   │                     │
│                     │   │    TOP SCREEN       │
│                     │   │    (Touch: No)      │
│   BOTTOM SCREEN     │   │                     │
│   (Touch: Yes)      │   │     [400x240        │
│                     │   │      scaled]        │  
│                     │   │                     │
└─────────────────────┘   └─────────────────────┘
```

## 🚀 Testing

### Wired Display Testing
1. Connect HDMI/USB-C display to Android device
2. Launch AzaharPlus and start 3DS game
3. External display should auto-show 3DS top screen
4. Touch phone screen - only affects bottom screen

### DeX Testing
1. Connect to Samsung DeX (wired/wireless)
2. Same behavior as wired display
3. Presentation should appear in DeX desktop environment

### Wireless Testing (Future)
1. Enable casting in AzaharPlus settings
2. Grant screen recording permission
3. Use casting app (Chromecast/AirPlay) to display virtual screen

## 🔧 Development Notes

### Compilation Safety
- All classes use proper Android imports
- Lifecycle-aware resource management
- Error handling with try-catch blocks
- Extensive logging for debugging

### Architecture Benefits
- **Clean separation of concerns** - Adapter pattern allows easy extension
- **Lifecycle management** - Automatic cleanup on activity destroy
- **Surface abstraction** - Easy integration with different display types
- **Backwards compatibility** - No changes to existing single-display behavior

### Performance Considerations
- Surface sharing between displays minimizes memory overhead
- Native rendering pipeline leverages existing RendererBase dual-window support
- Touch filtering happens at Java layer before expensive JNI calls

## 📋 Commit Status

This scaffolding implementation is **ready to commit** and provides:

1. ✅ **Compile-safe code** - All syntax verified, imports correct
2. ✅ **Clean architecture** - Modular, extensible adapter pattern  
3. ✅ **Lifecycle integration** - Proper Android component lifecycle handling
4. ✅ **Native bridge** - JNI methods for surface management
5. ✅ **Input handling** - Framework for casting-aware touch filtering
6. ✅ **Logging infrastructure** - Comprehensive debug logging throughout

**Next development phase** will focus on connecting the casting surface to the existing `RendererBase::secondary_window` architecture and implementing actual dual-display rendering.