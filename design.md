# Frontend Redesign: Minimal Monochrome Style

Clean, minimal e-commerce design using only black, white, and gray tones.

## Design System

| Element | Color |
|---------|-------|
| Background | #FFFFFF (white) |
| Surface/Cards | #FAFAFA (off-white) |
| Primary CTA | #000000 (black) |
| Header | #000000 (black) |
| Text Primary | #000000 (black) |
| Text Secondary | #666666 (gray) |
| Borders | #E5E5E5 (light gray) |
| Hover States | #333333 (dark gray) |

---

## Proposed Changes

### theme.css

- Replace all colors with monochrome palette
- Remove gradients and glow effects
- Flat buttons with black background, white text
- White product cards with subtle gray borders
- Clean sans-serif typography (system fonts)
- Minimal hover effects (opacity or subtle gray)

### index.html

- Update meta theme-color to black
- Remove Google Fonts (use system fonts)
- Simplify hero text - remove tech jargon
- Customer-focused messaging

---

## Verification

1. Start API Gateway → navigate to `http://localhost:8080`
2. Verify: white background, black buttons, no colors
3. Check mobile responsiveness
