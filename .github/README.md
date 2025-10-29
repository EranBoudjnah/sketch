<img src="./assets/sketch-icon.png" alt="Sketch logo" title="Sketch" align="right" width="64" height="64" />

# Sketch ✏️

A Jetpack Compose UI library of components with a hand-drawn, sketch-style appearance.

Designed to bring playful, human touches to your Compose UIs while keeping them fully customizable and production-ready.

https://github.com/user-attachments/assets/56c42ec7-4ed5-45e5-89ec-19d6a91edee9

## Table of Contents

- [Key features](#key-features)
- [Installation](#installation)
- [Usage](#usage)
    - [Composables](#composables)
    - [Shapes](#shapes)
- [Customization](#customization)
- [Contributing](#contributing)
- [Support](#support)
- [Sponsor](#sponsor)
- [License](#license)

---

## Key features

| Feature | Description |
|----------|--------------|
| ✏️ **Sketch look & feel** | Adds a hand-drawn, playful aesthetic to your Compose components. |
| 🧩 **Composable components** | Works with all your standard Compose UI code. |
| 🎨 **Customizable styles** | Control stroke width, color, randomness, and more. |
| 💡 **Lightweight** | No dependencies beyond Jetpack Compose. |
| 🧱 **Modular** | Import only what you need. |

---

## Installation

Add Sketch to your **Gradle** project:

```kotlin
dependencies {
    implementation("com.mitteloupe.sketch:sketch:<latest-version>")
}
```
💡 Replace <latest-version> with the latest release from [Releases](https://github.com/EranBoudjnah/sketch/releases)

## Usage

Use Sketch components just like any other Compose UI elements.

```kotlin
@Composable
fun ExampleScreen() {
    com.mitteloupe.sketch.Card {
        Text(text = "Hello, Sketch!")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          shape = SketchCapsuleShape(),
          onClick = { /* TODO */ }
        ) {
            Text("Click Me!")
        }
    }
}
```

### Composables

- `Card` — sketchy card container
- `Checkbox` — checkbox with an sketched box
- `CircularProgressIndicator`, `LinearProgressIndicator` — range of sketchy progress indicators
- `HorizontalDivider`, `VerticalDivider` — sketched dividers
- `RadioButton` — a sketched radio button
- `SketchDialog` — dialog with sketchy outlines
- `Slider`, `RangeSlider` — sliders with sketched tracks and handles
- `Switch` — sketch-styled switch
- `TextField` — input field with sketched underline

### Shapes

Useful for turning common composables sketchy.

- `SketchCapsuleShape` — capsule-shaped sketch, with circles on the left and right
- `SketchCircleShape` — sketched circle
- `SketchRectangleShape` — plain rectangle with sketchy edges
- `SketchRoundedCornerShape` — sketched round-cornered rectangle

---

## Customization

Most Sketch composables conform to the Compose standards, and should be intuitive if you're familiar with the common composables.

## Contributing

Contributions to this project are welcome!
Check out the [Contributing Guidelines](https://github.com/EranBoudjnah/sketch/blob/main/.github/CONTRIBUTING.md) before submitting a pull request.

---

## Support

Reach out to me via my **[profile page](https://github.com/EranBoudjnah)**.

---

## Sponsor

[![Sponsor](https://img.shields.io/badge/Sponsor-%E2%99%A5-lightgrey?style=flat&logo=githubsponsors)](https://github.com/sponsors/EranBoudjnah)

---

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-lightgrey.svg)](https://www.tldrlegal.com/license/mit-license)
