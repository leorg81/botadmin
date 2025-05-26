// index.js (resumen)
const { Client, LocalAuth } = require('whatsapp-web.js');
const qrcode = require('qrcode-terminal');
const axios = require('axios');

const API = 'http://localhost:8081/api/bot';

const client = new Client({ authStrategy: new LocalAuth(), puppeteer: { args: ['--no-sandbox'] } });

client.on('qr', qr => qrcode.generate(qr, { small: true }));
client.on('ready', () => console.log('✅ Bot listo'));

client.on('message', async (msg) => {
  const tel = msg.from.replace('@c.us','');
  try {
    // 1) si el usuario manda algo por primera vez, pedimos el prompt actual
    if (/^hola$|^menu$|^inicio$/i.test(msg.body.trim())) {
      const p = await axios.get(`${API}/prompt`, { params:{ tel } });
      return client.sendMessage(msg.from, p.data.mensaje);
    }
    // 2) enviamos su respuesta y mostramos el siguiente mensaje
    const r = await axios.post(`${API}/answer?tel=${tel}`, { respuesta: msg.body.trim() });
    return client.sendMessage(msg.from, r.data.mensaje);
  } catch (e) {
    console.error(e?.response?.data || e.message);
    client.sendMessage(msg.from, "Ocurrió un error. Escriba 'menu' para reiniciar.");
  }
});

client.initialize();
