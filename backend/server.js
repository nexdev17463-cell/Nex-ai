import express from 'express';
import rateLimit from 'express-rate-limit';

const app = express();
app.disable('x-powered-by');
app.use(express.json({ limit: '8mb' }));
app.use('/api/', rateLimit({ windowMs: 60_000, limit: 20, standardHeaders: true, legacyHeaders: false }));
app.get('/health', (_req, res) => res.json({ ok: true, service: 'NEX AI backend' }));

app.post('/api/chat', async (req, res) => {
  const message = typeof req.body?.message === 'string' ? req.body.message.trim() : '';
  const provider = req.body?.provider === 'openai' ? 'openai' : 'gemini';
  if (!message || message.length > 12000) return res.status(400).json({ error: 'Message is required (max 12000 characters).' });
  try {
    let reply;
    if (provider === 'openai') {
      if (!process.env.OPENAI_API_KEY) return res.status(503).json({ error: 'OpenAI provider is not configured on the server.' });
      const r = await fetch('https://api.openai.com/v1/responses', {
        method: 'POST', headers: { Authorization: `Bearer ${process.env.OPENAI_API_KEY}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ model: process.env.OPENAI_TEXT_MODEL || 'gpt-4.1-mini', input: message })
      });
      const data = await r.json();
      if (!r.ok) return res.status(502).json({ error: data.error?.message || 'OpenAI request failed.' });
      reply = data.output?.flatMap(x => x.content || []).filter(x => x.type === 'output_text').map(x => x.text).join('\n') || 'No text response received.';
    } else {
      if (!process.env.GEMINI_API_KEY) return res.status(503).json({ error: 'Gemini provider is not configured on the server.' });
      const model = process.env.GEMINI_TEXT_MODEL || 'gemini-2.5-flash';
      const r = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(model)}:generateContent`, {
        method: 'POST', headers: { 'x-goog-api-key': process.env.GEMINI_API_KEY, 'Content-Type': 'application/json' },
        body: JSON.stringify({ contents: [{ parts: [{ text: message }] }] })
      });
      const data = await r.json();
      if (!r.ok) return res.status(502).json({ error: data.error?.message || 'Gemini request failed.' });
      reply = data.candidates?.[0]?.content?.parts?.map(x => x.text || '').join('') || 'No text response received.';
    }
    return res.json({ provider, reply });
  } catch (error) {
    console.error('chat request failed:', error?.message || 'unknown error');
    return res.status(502).json({ error: 'AI provider could not be reached.' });
  }
});

app.post('/api/image', async (req, res) => {
  const prompt = typeof req.body?.prompt === 'string' ? req.body.prompt.trim() : '';
  if (!prompt || prompt.length > 4000) return res.status(400).json({ error: 'Prompt is required (max 4000 characters).' });
  if (!process.env.OPENAI_API_KEY) return res.status(503).json({ error: 'OpenAI image provider is not configured on the server.' });
  try {
    const r = await fetch('https://api.openai.com/v1/images/generations', {
      method: 'POST', headers: { Authorization: `Bearer ${process.env.OPENAI_API_KEY}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({ model: process.env.OPENAI_IMAGE_MODEL || 'gpt-image-1', prompt, size: '1024x1024' })
    });
    const data = await r.json();
    if (!r.ok) return res.status(502).json({ error: data.error?.message || 'Image generation failed.' });
    const image = data.data?.[0]?.b64_json;
    if (!image) return res.status(502).json({ error: 'Provider returned no image data.' });
    return res.json({ imageBase64: image, mimeType: 'image/png' });
  } catch (error) {
    console.error('image request failed:', error?.message || 'unknown error');
    return res.status(502).json({ error: 'Image provider could not be reached.' });
  }
});

const port = Number(process.env.PORT || 8080);
app.listen(port, '0.0.0.0', () => console.log(`NEX AI backend listening on ${port}`));
