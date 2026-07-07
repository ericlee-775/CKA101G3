require('dotenv').config();
const express = require('express');
const fetch = require('node-fetch');
const app = express();

app.use(express.json());

app.post('/api/claude', async (req, res) => {
  try {
    const prompt = req.body.prompt || '';
    const response = await fetch('https://api.anthropic.com/v1/complete', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${process.env.CLAUDE_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'claude-2.1',
        prompt,
        max_tokens_to_sample: 300
      })
    });
    const data = await response.json();
    res.json(data);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: err.message });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Claude proxy listening on http://localhost:${port}`));
