# Protótipo de UI (mockup HTML)

Protótipo HTML interativo de alta fidelidade do app, usado para iterar o visual
antes de portar para Jetpack Compose.

- `index-v2.html` — layout **V2** (atual): volume vertical à direita, mute como
  botão grande central + **play/pause** secundário. **É o padrão servido na raiz.**
- `index.html` — layout V1 (anterior), mantido para referência.
- `icon.html` — estudo do ícone do app.
- `serve.js` — servidor estático Node (sem dependências).

## Rodar

```bash
node serve.js        # http://localhost:4599  (/ → index-v2.html)
```

Ou abrir o `.html` direto no navegador (funciona offline).
