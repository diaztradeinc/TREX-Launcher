import { useState, useEffect, useRef } from 'react'

/* ── tiny shared pieces ── */

function Clock() {
  const [t, setT] = useState(new Date())
  useEffect(() => { const id = setInterval(() => setT(new Date()), 1000); return () => clearInterval(id) }, [])
  const h = t.getHours() % 12 || 12, m = String(t.getMinutes()).padStart(2, '0')
  return <span>{h}:{m} {t.getHours() >= 12 ? 'PM' : 'AM'}</span>
}

function Tab({ active, onClick, children }) {
  return <button className={`rl-tab${active ? ' active' : ''}`} onClick={onClick}>{children}</button>
}
function Chip({ active, onClick, children }) {
  return <button className={`rl-chip${active ? ' active' : ''}`} onClick={onClick}>{children}</button>
}
function Toggle({ on, onClick }) {
  return <button className={`rl-toggle${on ? ' on' : ''}`} onClick={onClick}><span className="rl-toggle-k" /></button>
}
function Metric({ label, value, unit, glow }) {
  return (
    <div className={`rl-metric${glow ? ' glow' : ''}`}>
      <div className="rl-metric-lbl">{label}</div>
      <div className="rl-metric-row"><span className="rl-metric-val">{value}</span><span className="rl-metric-unit">{unit}</span></div>
    </div>
  )
}
function SliderRow({ label, icon, min, max, value, onChange, display }) {
  return (
    <div className="rl-slider-row">
      <span className="rl-slider-lbl">{icon || label}</span>
      <input type="range" min={min} max={max} value={value} onChange={e => onChange(+e.target.value)} className="rl-slider" />
      <span className="rl-slider-val">{display || value}</span>
    </div>
  )
}

/* ── SPLASH ── */

const perms = [
  { icon: '\u{1F4F1}', title: 'Phone & Contacts', desc: 'Hands-free calling and navigation to saved contacts.' },
  { icon: '\u{1F4CD}', title: 'Location Services', desc: 'GPS navigation, live traffic, and 0\u201360 timing.' },
  { icon: '\u{1F4E2}', title: 'Notifications', desc: 'Incoming messages, calls, and media controls.' },
  { icon: '\u{1F3A4}', title: 'Microphone', desc: 'Voice search for navigation and media.' },
  { icon: '\u{1F4BB}', title: 'Display Overlay', desc: 'Show the launcher over the Uconnect system.' },
  { icon: '\u{1F50C}', title: 'Bluetooth & OBD-II', desc: 'Connect to OBDLink MX+ for live vehicle data.' },
]

function Splash({ onDone }) {
  const [phase, setPhase] = useState('logo')
  const [pi, setPi] = useState(0)
  const [granted, setGranted] = useState([])
  const [isDefault, setIsDefault] = useState(false)

  useEffect(() => {
    if (phase === 'logo') { const t = setTimeout(() => setPhase('welcome'), 2800); return () => clearTimeout(t) }
    if (phase === 'done') onDone()
  }, [phase])

  const grant = () => {
    setGranted([...granted, pi])
    if (pi < perms.length - 1) setPi(pi + 1)
    else { setPhase('final'); setTimeout(() => setPhase('done'), 1600) }
  }

  if (phase === 'logo') return (
    <div className="sp-logo">
      <div className="sp-logo-anim">
        <div className="sp-ram">RAM</div>
        <div className="sp-trx">TRX</div>
        <div className="sp-edition">REDLINE EDITION</div>
      </div>
      <div className="sp-bar"><div className="sp-bar-fill" /></div>
      <div className="sp-ver">v6.0</div>
    </div>
  )

  if (phase === 'welcome') return (
    <div className="sp-welcome">
      <div className="sp-welcome-bg" />
      <div className="sp-welcome-body">
        <div className="sp-badge">RAM 1500 TRX</div>
        <div className="sp-hero-title">REDLINE</div>
        <div className="sp-hero-sub">Your truck. Your rules.</div>
        <div className="sp-features">
          {['\u{1F4CD} Google Navigation', '\u{1F3B5} Media Control', '\u{1F50C} OBD-II Live Data', '\u23F1 0\u201360 Timer'].map((f, i) =>
            <div key={i} className="sp-feat">{f}</div>
          )}
        </div>
        <button className="sp-start" onClick={() => { setPhase('perms'); setPi(0) }}>GET STARTED</button>
        <button className={`sp-default${isDefault ? ' set' : ''}`} onClick={() => setIsDefault(!isDefault)}>
          {isDefault ? '\u2713 DEFAULT LAUNCHER SET' : 'SET AS DEFAULT LAUNCHER'}
        </button>
        <div className="sp-foot">Designed for Uconnect 5 • 12″ Display</div>
      </div>
    </div>
  )

  if (phase === 'perms') {
    const p = perms[pi]
    return (
      <div className="sp-perm">
        <div className="sp-perm-progress"><div className="sp-perm-fill" style={{ width: `${(pi / perms.length) * 100}%` }} /></div>
        <div className="sp-perm-dots">
          {perms.map((_, i) => <div key={i} className={`sp-dot${i < pi ? ' done' : ''}${i === pi ? ' cur' : ''}`}>{i < pi ? '\u2713' : i + 1}</div>)}
        </div>
        <div className="sp-perm-card">
          <div className="sp-perm-icon">{p.icon}</div>
          <div className="sp-perm-title">{p.title}</div>
          <div className="sp-perm-desc">{p.desc}</div>
          <div className="sp-perm-mock">
            <div className="sp-perm-mock-label">ALLOW TRX LAUNCHER TO ACCESS</div>
            <div className="sp-perm-mock-val">{p.title.toUpperCase()}</div>
            <div className="sp-perm-opt sel">Allow while using the app</div>
            <div className="sp-perm-opt">Ask every time</div>
            <div className="sp-perm-opt">Don’t allow</div>
          </div>
          <div className="sp-perm-btns">
            <button className="sp-perm-skip" onClick={grant}>SKIP</button>
            <button className="sp-perm-allow" onClick={grant}>ALLOW</button>
          </div>
        </div>
      </div>
    )
  }

  if (phase === 'final') return (
    <div className="sp-final">
      <div className="sp-spinner" />
      <div className="sp-final-text">Setting up your launcher</div>
      <div className="sp-final-sub">Configuring permissions and preferences…</div>
    </div>
  )
  return null
}

/* ── HOME SCREEN ── */

function HomeScreen({ goTo, quickApps, setQuickApps }) {
  const gauges = [
    { label: 'RPM', value: '1,250', unit: 'RPM', pct: 22 },
    { label: 'BOOST', value: '0.0', unit: 'PSI', pct: 4 },
    { label: 'TEMP', value: '190', unit: '°F', pct: 55 },
    { label: 'HP', value: '702', unit: 'HP', pct: 100 },
  ]

  return (
    <div className="rl-screen">
      <div className="rl-pad home-render">
        <div className="home-hdr">
          <div className="home-badge">PRECISION COMMAND</div>
          <div className="home-title">Focused on the road.</div>
        </div>

        <div className="home-tiles">
          <div className="rl-card home-nav-tile" onClick={() => goTo(1)}>
            <div className="rl-card-top"><span className="rl-dot green" />NAVIGATION</div>
            <div className="home-nav-map">
              <img className="home-nav-map-img" src="/nav-map-bg.webp" alt="" />
              <span className="home-nav-pin"><svg viewBox="0 0 24 24" width="20" height="20" fill="var(--r)"><path d="M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7zm0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5z" /></svg></span>
            </div>
            <div className="home-nav-cta">OPEN MAP <span className="home-nav-arrow">&#8250;</span></div>
          </div>
          <div className="rl-card home-np-tile" onClick={() => goTo(2)}>
            <div className="rl-card-top"><span className="rl-dot green" />NOW PLAYING</div>
            <div className="home-np-art"><img className="media-art-img" src="/music-art.webp" alt="" /></div>
            <div className="home-np-title">NOTHING PLAYING</div>
            <div className="home-np-artist">Pick a track to get started</div>
            <button className="rl-btn sm" onClick={e => { e.stopPropagation(); goTo(2) }}>MEDIA</button>
          </div>
        </div>

        <div className="home-gauges">
          {gauges.map(g => (
            <div key={g.label} className="gauge">
              <div className="gauge-ring" style={{ background: `conic-gradient(var(--r) 0 ${g.pct}%, #1c1e22 ${g.pct}% 100%)` }}>
                <span className="gauge-val">{g.value}</span>
              </div>
              <div className="gauge-label">{g.label}</div>
              <div className="gauge-unit">{g.unit}</div>
            </div>
          ))}
        </div>

        <QuickAppsBar quickApps={quickApps} setQuickApps={setQuickApps} goTo={goTo} />
      </div>
    </div>
  )
}

function QuickAppsBar({ quickApps, setQuickApps, goTo }) {
  const [picker, setPicker] = useState(false)
  const quick = quickApps.map(name => apps.find(a => a[1] === name)).filter(Boolean)
  const toggle = name => {
    setQuickApps(q => q.includes(name) ? q.filter(x => x !== name) : q.length >= 6 ? q : [...q, name])
  }

  return (
    <div className="home-quick">
      <div className="home-quick-hdr">
        <span className="rl-card-top">QUICK APPS</span>
        <button className="home-quick-add" onClick={() => setPicker(true)}>+ ADD</button>
      </div>
      <div className="home-quick-row">
        {quick.map((a, i) => (
          <button key={i} className="home-quick-app" onClick={() => goTo(4)}>
            <span className="home-quick-icon" style={{ background: a[3] }}>{a[0]}</span>
            <span className="home-quick-name">{a[1]}</span>
          </button>
        ))}
        {quick.length < 6 && (
          <button className="home-quick-app add" onClick={() => setPicker(true)}>
            <span className="home-quick-icon add">+</span>
            <span className="home-quick-name">Add</span>
          </button>
        )}
      </div>

      {picker && (
        <div className="home-quick-picker-overlay" onClick={() => setPicker(false)}>
          <div className="home-quick-picker" onClick={e => e.stopPropagation()}>
            <div className="home-quick-picker-hdr">
              <span>CHOOSE QUICK APPS</span>
              <button className="home-quick-picker-close" onClick={() => setPicker(false)}>&times;</button>
            </div>
            <div className="home-quick-picker-list">
              {apps.map((a, i) => {
                const on = quickApps.includes(a[1])
                return (
                  <button key={i} className={`home-quick-picker-item${on ? ' on' : ''}`} onClick={() => toggle(a[1])}>
                    <span className="home-quick-picker-check">{on ? '\u2713' : '+'}</span>
                    <span className="home-quick-icon" style={{ background: a[3] }}>{a[0]}</span>
                    <span className="home-quick-picker-name">{a[1]}</span>
                  </button>
                )
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
/* ── NAVIGATION SCREEN (Google Maps style) ── */

const places = [
  { name: 'Home', addr: '123 Main St, Plainsboro, NJ', icon: 'H', fav: true },
  { name: 'Work', addr: '500 Tech Way, Princeton, NJ', icon: 'W', fav: true },
  { name: 'Wawa', addr: 'Washington Rd, Plainsboro', icon: 'W', fav: true },
  { name: 'ShopRite', addr: 'Schalks Crossing Rd', icon: 'S', fav: false },
  { name: 'Princeton University', addr: 'Princeton, NJ', icon: 'P', fav: false },
  { name: 'Newark Airport', addr: 'Newark, NJ', icon: 'N', fav: false },
  { name: 'Costco', addr: 'South Brunswick, NJ', icon: 'C', fav: false },
  { name: 'Gym', addr: 'Fitness Way, Plainsboro', icon: 'G', fav: false },
]
const cats = ['Gas', 'Food', 'Grocery', 'Coffee', 'Hospital', 'Parking']

function NavScreen({ settings, setSettings, goTo }) {
  const [tab, setTab] = useState(0)
  const [q, setQ] = useState('')
  const [focused, setFocused] = useState(false)
  const [selectedPlace, setSelectedPlace] = useState(null)
  const [traffic, setTraffic] = useState(true)
  const [mapType, setMapType] = useState(0)
  const [ttMode, setTtMode] = useState(false)
  const [muted, setMuted] = useState(false)
  const tabs = ['Map', 'Recents', 'Favorites', 'Settings']

  const matches = q.trim().length >= 2 ? places.filter(p =>
    p.name.toLowerCase().includes(q.toLowerCase()) || p.addr.toLowerCase().includes(q.toLowerCase())
  ) : []

  const selectPlace = (p) => { setSelectedPlace(p); setQ(''); setFocused(false) }
  const startNav = () => { setTtMode(true) }
  const stopNav = () => { setTtMode(false); setSelectedPlace(null) }

  return (
    <div className="rl-screen">
      <div className="rl-section-hdr nav-hdr">
        <div className="rl-section-badge">PRECISION NAVIGATION</div>
        <div className="rl-section-title">Google Maps SDK</div>
      </div>
      <div className="rl-pad">
        <div className="rl-tabs">{tabs.map((t, i) => <Tab key={i} active={tab === i} onClick={() => setTab(i)}>{t}</Tab>)}</div>

        {tab === 0 && (
          <div className="nav-screen">
            <div className={`nav-search-wrap${focused ? ' focused' : ''}`}>
              <svg className="nav-search-icon" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><circle cx="11" cy="11" r="7" /><line x1="16.5" y1="16.5" x2="21" y2="21" /></svg>
              <input className="nav-search" placeholder="Search here" value={q}
                onChange={e => setQ(e.target.value)} onFocus={() => setFocused(true)} onBlur={() => setTimeout(() => setFocused(false), 200)} />
              {q && <button className="nav-clear" onClick={() => setQ('')}>&times;</button>}
              <button className="nav-mic">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"><rect x="9" y="3" width="6" height="11" rx="3" /><path d="M5 11a7 7 0 0 0 14 0" /><line x1="12" y1="18" x2="12" y2="21" /></svg>
              </button>
            </div>

            {focused && matches.length > 0 && (
              <div className="nav-results">
                {matches.map((p, i) => (
                  <button key={i} className="nav-result" onClick={() => selectPlace(p)}>
                    <span className="nav-result-icon">{p.icon}</span>
                    <span className="nav-result-info"><span className="nav-result-name">{p.name}</span><span className="nav-result-addr">{p.addr}</span></span>
                  </button>
                ))}
              </div>
            )}

            {!ttMode && !selectedPlace && (
              <div className="nav-cats">{cats.map((c, i) => <button key={i} className="nav-cat">{c}</button>)}</div>
            )}

            <div className="nav-map">
              <div className="nav-map-inner">
                {ttMode ? (
                  <div className="nav-tt">
                    <div className="nav-tt-bar">
                      <div className="nav-tt-dir">
                        <svg className="nav-tt-arrow" viewBox="0 0 24 24" width="26" height="26" fill="currentColor"><path d="M12 2l8 18h-5l-3-6-3 6H4z" /></svg>
                        <span className="nav-tt-text">Continue on US-1 North</span>
                      </div>
                      <div className="nav-tt-dist">2.4 mi</div>
                    </div>
                    <div className="nav-tt-eta">
                      <span className="nav-tt-time">14 min</span>
                      <span className="nav-tt-arrive">Arrive 3:42 PM &bull; 7.2 mi</span>
                    </div>
                    <div className="nav-tt-road">
                      <div className="nav-tt-speed-limit"><span className="nav-tt-sl-num">45</span><span className="nav-tt-sl-unit">MPH</span></div>
                      <div className="nav-tt-current-speed">38<span className="nav-tt-cs-unit"> mph</span></div>
                    </div>
                    <div className="nav-tt-actions">
                      <button className={`nav-act${muted ? ' active' : ''}`} onClick={() => setMuted(!muted)}>Mute</button>
                      <button className="nav-act">Search</button>
                      <button className="nav-act">Overview</button>
                      <button className="nav-act end" onClick={stopNav}>End</button>
                    </div>
                  </div>
                ) : selectedPlace ? (
                  <div className="nav-place-card">
                    <div className="nav-place-icon">{selectedPlace.icon}</div>
                    <div className="nav-place-name">{selectedPlace.name}</div>
                    <div className="nav-place-addr">{selectedPlace.addr}</div>
                    <div className="nav-place-eta">14 min &bull; 7.2 mi &bull; via US-1 N</div>
                    <div className="nav-place-actions">
                      <button className="rl-btn pri" onClick={startNav}>START</button>
                      <button className="rl-btn" onClick={() => setSelectedPlace(null)}>CANCEL</button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="nav-map-label">MAP</div>
                    <div className="nav-map-text">Google Maps Navigation SDK</div>
                    <div className="nav-map-sub">Live traffic &bull; Turn-by-turn &bull; Speed limits</div>
                  </>
                )}
              </div>

              <div className="nav-map-controls">
                <button className="nav-ctrl-btn" title="Recenter"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="7" /><line x1="12" y1="2" x2="12" y2="6" /><line x1="12" y1="18" x2="12" y2="22" /><line x1="2" y1="12" x2="6" y2="12" /><line x1="18" y1="12" x2="22" y2="12" /></svg></button>
                <button className="nav-ctrl-btn" title="Compass"><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="12" cy="12" r="9" /><path d="M15 9l-3 6-3-6 3-3z" fill="currentColor" stroke="none" /></svg></button>
                <button className={`nav-ctrl-btn${traffic ? ' active' : ''}`} title="Traffic" onClick={() => setTraffic(!traffic)}><svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><rect x="9" y="3" width="6" height="18" rx="2" /><circle cx="12" cy="8" r="1.6" /><circle cx="12" cy="13" r="1.6" /><circle cx="12" cy="18" r="1.6" /></svg></button>
                <button className="nav-ctrl-btn" title="3D" onClick={() => setMapType(mapType ? 0 : 1)}>{mapType ? '3D' : '2D'}</button>
              </div>

              {!ttMode && (
                <div className="nav-bottom-bar">
                  <div className="nav-bb-speed"><span className="nav-bb-num">0</span><span className="nav-bb-unit">MPH</span></div>
                  <div className="nav-bb-compass">N</div>
                </div>
              )}
            </div>

            <div className="nav-music-card">
              <div className="rl-card-top">QUEUED MUSIC</div>
              <div className="nav-music-row">
                <div className="nav-music-info">
                  <div className="nav-music-title">{queue[0].title}</div>
                  <div className="nav-music-artist">{queue[0].artist}</div>
                </div>
                <button className="nav-music-play" onClick={() => goTo(2)}>&#9654;</button>
              </div>
            </div>

            {!ttMode && !selectedPlace && (
              <div className="nav-quick-row">
                {places.filter(p => p.fav).map((p, i) => (
                  <button key={i} className="nav-quick-btn" onClick={() => selectPlace(p)}>
                    <span className="nav-quick-icon">{p.icon}</span>
                    <span className="nav-quick-name">{p.name}</span>
                  </button>
                ))}
              </div>
            )}
          </div>
        )}

        {tab === 1 && (
          <div className="nav-list">
            {places.map((p, i) => (
              <button key={i} className="nav-list-item" onClick={() => { selectPlace(p); setTab(0) }}>
                <span className="nav-li-icon">{p.icon}</span>
                <span className="nav-li-info"><span className="nav-li-name">{p.name}</span><span className="nav-li-addr">{p.addr}</span></span>
                <span className="nav-li-go">&#8250;</span>
              </button>
            ))}
          </div>
        )}

        {tab === 2 && (
          <div className="nav-list">
            {places.filter(p => p.fav).map((p, i) => (
              <button key={i} className="nav-list-item" onClick={() => { selectPlace(p); setTab(0) }}>
                <span className="nav-li-icon">{p.icon}</span>
                <span className="nav-li-info"><span className="nav-li-name">{p.name}</span><span className="nav-li-addr">{p.addr}</span></span>
                <span className="nav-li-go">&#8250;</span>
              </button>
            ))}
          </div>
        )}

        {tab === 3 && (
          <div className="nav-settings">
            <div className="rl-card">
              <div className="rl-card-top">MAP TYPE</div>
              <div className="rl-chip-row">
                {['Default', 'Satellite', 'Terrain', 'Dark'].map((m, i) =>
                  <Chip key={i} active={settings.navMapStyle === i} onClick={() => setSettings({ ...settings, navMapStyle: i })}>{m}</Chip>
                )}
              </div>
            </div>
            <div className="rl-card">
              <div className="rl-card-top">ROUTE PREFERENCES</div>
              <div className="rl-chip-row">
                {['Fastest', 'Shortest', 'Avoid Tolls', 'Avoid Highways'].map((r, i) =>
                  <Chip key={i} active={settings.navRoutePref === i} onClick={() => setSettings({ ...settings, navRoutePref: i })}>{r}</Chip>
                )}
              </div>
            </div>
            <div className="rl-card">
              <div className="rl-card-top">OPTIONS</div>
              <div className="rl-toggle-row"><span>Voice Guidance</span><Toggle on={settings.navVoice !== false} onClick={() => setSettings({ ...settings, navVoice: !settings.navVoice })} /></div>
              <div className="rl-toggle-row"><span>Speed Limit Alerts</span><Toggle on={settings.navSpeedAlert !== false} onClick={() => setSettings({ ...settings, navSpeedAlert: !settings.navSpeedAlert })} /></div>
              <div className="rl-toggle-row"><span>Traffic Layer</span><Toggle on={settings.navTraffic !== false} onClick={() => setSettings({ ...settings, navTraffic: !settings.navTraffic })} /></div>
              <div className="rl-toggle-row"><span>Satellite View</span><Toggle on={!!settings.navSat} onClick={() => setSettings({ ...settings, navSat: !settings.navSat })} /></div>
              <div className="rl-toggle-row"><span>Night Mode</span><Toggle on={settings.navNight !== false} onClick={() => setSettings({ ...settings, navNight: !settings.navNight })} /></div>
            </div>
            <div className="rl-card">
              <div className="rl-card-top">NAVIGATION UNITS</div>
              <div className="rl-chip-row">
                {['Miles', 'Kilometers'].map((u, i) =>
                  <Chip key={i} active={(settings.navUnits || 0) === i} onClick={() => setSettings({ ...settings, navUnits: i })}>{u}</Chip>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

/* ── MEDIA SCREEN ── */

const queue = [
  { title: 'Thunderstruck', artist: 'AC/DC', dur: '4:52' },
  { title: 'Enter Sandman', artist: 'Metallica', dur: '5:31' },
  { title: 'Highway to Hell', artist: 'AC/DC', dur: '3:28' },
  { title: 'Back in Black', artist: 'AC/DC', dur: '4:15' },
  { title: 'Fuel', artist: 'Metallica', dur: '4:28' },
  { title: 'Symphony of Destruction', artist: 'Megadeth', dur: '5:02' },
  { title: 'Crazy Train', artist: 'Ozzy Osbourne', dur: '4:56' },
  { title: 'Breaking the Law', artist: 'Judas Priest', dur: '2:38' },
  { title: 'Run to the Hills', artist: 'Iron Maiden', dur: '3:54' },
  { title: 'War Pigs', artist: 'Black Sabbath', dur: '7:57' },
]

const artBg = i => {
  const g = [
    ['#e1192d', '#4a0a12'], ['#d99a2b', '#5c3d0a'], ['#2b7de1', '#0c2a52'],
    ['#e16a1f', '#5c2a0a'], ['#1db954', '#0a3d1d'], ['#8b5cf6', '#2c1a5e'],
    ['#34c759', '#0d3d1d'], ['#ff9500', '#5c3400'], ['#5ac8fa', '#123a4d'],
    ['#ff3b30', '#5c120d'],
  ]
  const [a, b] = g[i % g.length]
  return `linear-gradient(135deg, ${a}, ${b})`
}

const NoteGlyph = ({ size = 16 }) => (
  <svg viewBox="0 0 24 24" width={size} height={size} fill="currentColor"><path d="M9 18V5l12-2v13" /><circle cx="6" cy="18" r="3" /><circle cx="18" cy="16" r="3" /></svg>
)

function MediaScreen({ settings, setSettings }) {
  const [tab, setTab] = useState(0)
  const [playing, setPlaying] = useState(false)
  const [cur, setCur] = useState(0)
  const [prog, setProg] = useState(0)
  const [shuffle, setShuffle] = useState(false)
  const [repeat, setRepeat] = useState(false)
  const tabs = ['Now Playing', 'Queue', 'Sources', 'Audio']

  useEffect(() => {
    if (!playing) return
    const id = setInterval(() => setProg(p => { if (p >= 100) { setPlaying(false); return 0 }; return p + 0.5 }), 200)
    return () => clearInterval(id)
  }, [playing])

  const next = () => { setCur((cur + 1) % queue.length); setProg(0) }
  const prev = () => { setCur((cur - 1 + queue.length) % queue.length); setProg(0) }
  const pick = i => { setCur(i); setProg(0); setPlaying(true); setTab(0) }
  const t = queue[cur]

  return (
    <div className="rl-screen">
      <div className="rl-section-hdr media-hdr">
        <div className="rl-section-badge">PRECISION MEDIA</div>
        <div className="rl-section-title">Your music, centered.</div>
      </div>
      <div className="rl-pad">
        <div className="rl-tabs">{tabs.map((x, i) => <Tab key={i} active={tab === i} onClick={() => setTab(i)}>{x}</Tab>)}</div>

        {tab === 0 && (
          <div className="media-np">
            <div className="media-art" style={{ background: artBg(cur) }}>
              {!playing
                ? <img className="media-art-img" src="/music-art.webp" alt="" />
                : <span className="media-art-note"><NoteGlyph size={56} /></span>}
            </div>
            <div className="media-artist">{playing ? t.artist.toUpperCase() : 'NOTHING PLAYING'}</div>
            <div className="media-title">{playing ? t.title : 'Pick a track to get started'}</div>
            <div className="media-controls">
              <button className={`mc-btn${shuffle ? ' on' : ''}`} onClick={() => setShuffle(!shuffle)}>
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M16 3h5v5" /><path d="M4 20L21 3" /><path d="M21 16v5h-5" /><path d="M15 15l6 6" /><path d="M4 4l5 5" /></svg>
              </button>
              <button className="mc-btn" onClick={prev}>
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M6 5h2v14H6z" /><path d="M20 5v14l-10-7z" /></svg>
              </button>
              <button className="mc-btn play" onClick={() => setPlaying(!playing)}>
                {playing
                  ? <svg viewBox="0 0 24 24" width="26" height="26" fill="currentColor"><path d="M7 5h4v14H7z" /><path d="M13 5h4v14h-4z" /></svg>
                  : <svg viewBox="0 0 24 24" width="26" height="26" fill="currentColor"><path d="M8 5v14l11-7z" /></svg>}
              </button>
              <button className="mc-btn" onClick={next}>
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M16 5h2v14h-2z" /><path d="M4 5v14l10-7z" /></svg>
              </button>
              <button className={`mc-btn${repeat ? ' on' : ''}`} onClick={() => setRepeat(!repeat)}>
                <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 1l4 4-4 4" /><path d="M3 11V9a4 4 0 0 1 4-4h14" /><path d="M7 23l-4-4 4-4" /><path d="M21 13v2a4 4 0 0 1-4 4H3" /></svg>
              </button>
            </div>
            <div className="media-prog">
              <div className="media-prog-bar"><div className="media-prog-fill" style={{ width: `${prog}%` }} /></div>
              <div className="media-prog-times">
                <span>{Math.floor(prog * 0.05 * 60)}:{String(Math.floor((prog * 3) % 60)).padStart(2, '0')}</span>
                <span>{t.dur}</span>
              </div>
            </div>
            <div className="media-queue-preview">
              <div className="rl-card-top">UP NEXT</div>
              <div className="media-q-scroll">
                {queue.map((tr, i) => (
                  <button key={i} className={`mq-item${i === cur ? ' cur' : ''}`} onClick={() => pick(i)}>
                    <span className="mq-art" style={{ background: artBg(i) }}><NoteGlyph size={14} /></span>
                    <span className="mq-info"><span className="mq-title">{tr.title}</span><span className="mq-artist">{tr.artist}</span></span>
                    <span className="mq-dur">{tr.dur}</span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}

        {tab === 1 && (
          <div className="media-queue-full">
            <div className="rl-card-top">FULL QUEUE &mdash; {queue.length} TRACKS</div>
            <div className="media-q-scroll full">
              {queue.map((tr, i) => (
                <button key={i} className={`mq-item${i === cur ? ' cur' : ''}`} onClick={() => pick(i)}>
                  <span className="mq-art" style={{ background: artBg(i) }}><NoteGlyph size={14} /></span>
                  <span className="mq-info"><span className="mq-title">{tr.title}</span><span className="mq-artist">{tr.artist}</span></span>
                  <span className="mq-dur">{tr.dur}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {tab === 2 && (
          <div className="media-sources">
            {[
              ['YouTube Music', true], ['Spotify', false],
              ['Pandora', false], ['Phone Audio', false],
              ['FM Radio', false], ['SiriusXM', false],
            ].map((s, i) => (
              <button key={i} className={`rl-btn source-btn${s[1] ? ' pri' : ''}`}>{s[0]}</button>
            ))}
          </div>
        )}

        {tab === 3 && (
          <div className="media-audio">
            <div className="rl-card">
              <div className="rl-card-top">MASTER VOLUME</div>
              <SliderRow label="VOL" min={0} max={100} value={settings.vol} onChange={v => setSettings({ ...settings, vol: v })} />
            </div>
            <div className="rl-card">
              <div className="rl-card-top">EQUALIZER</div>
              <SliderRow label="BASS" min={0} max={10} value={settings.bass} onChange={v => setSettings({ ...settings, bass: v })} display={`+${settings.bass}`} />
              <SliderRow label="TREBLE" min={0} max={10} value={settings.treble} onChange={v => setSettings({ ...settings, treble: v })} display={`+${settings.treble}`} />
              <SliderRow label="BAL" min={-10} max={10} value={settings.bal} onChange={v => setSettings({ ...settings, bal: v })}
                display={settings.bal > 0 ? `R+${settings.bal}` : settings.bal < 0 ? `L+${-settings.bal}` : 'CTR'} />
            </div>
            <div className="rl-card">
              <div className="rl-card-top">SOUND MODE</div>
              <div className="rl-chip-row">
                {['Auto', 'Surround', 'Stereo', 'Party'].map((m, i) =>
                  <Chip key={i} active={(settings.soundMode || 0) === i} onClick={() => setSettings({ ...settings, soundMode: i })}>{m}</Chip>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

/* ── PERFORMANCE SCREEN ── */

function PerfScreen({ settings, setSettings }) {
  const [tab, setTab] = useState(0)
  const [running, setRunning] = useState(false)
  const [timer, setTimer] = useState(0)
  const ref = useRef(null)
  const tabs = ['Live Data', '0\u201360', 'Gauges', 'History']
  const [history, setHistory] = useState([
    { n: 'Run 1', t: '3.72', d: '06/15/25', m: 'Sport' },
    { n: 'Run 2', t: '3.91', d: '06/20/25', m: 'Sport' },
    { n: 'Run 3', t: '3.58', d: '07/01/25', m: 'Baja' },
    { n: 'Run 4', t: '4.12', d: '07/14/25', m: 'Street' },
  ])

  const startTimer = () => {
    if (running) { clearInterval(ref.current); setRunning(false); return }
    setTimer(0); setRunning(true)
    const s = Date.now()
    ref.current = setInterval(() => {
      const e = (Date.now() - s) / 1000
      setTimer(e)
      if (e >= 3.7) {
        clearInterval(ref.current); setRunning(false)
        setHistory(h => [...h, { n: `Run ${h.length + 1}`, t: e.toFixed(2), d: new Date().toLocaleDateString('en-US', { month: '2-digit', day: '2-digit', year: '2-digit' }), m: 'Sport' }])
      }
    }, 10)
  }
  useEffect(() => () => { if (ref.current) clearInterval(ref.current) }, [])

  const pids = [['RPM', '--', 'RPM'], ['COOLANT', '--', '\u00b0F'], ['INTAKE', '--', '\u00b0F'], ['BATTERY', '--', 'V'], ['LOAD', '--', '%'], ['TRANS', '--', '\u00b0F']]

  return (
    <div className="rl-screen">
      <div className="rl-section-hdr perf-hdr">
        <div className="rl-section-badge">PRECISION PERFORMANCE</div>
        <div className="rl-section-title">Every heartbeat of the truck.</div>
      </div>
      <div className="rl-pad">
        <div className="rl-tabs">{tabs.map((x, i) => <Tab key={i} active={tab === i} onClick={() => setTab(i)}>{x}</Tab>)}</div>
        <div className="perf-status"><span className="rl-dot red" />OBDLink MX+ • DISCONNECTED</div>

        {(tab === 0 || tab === 2) && (
          <div className="perf-grid">
            {pids.map((p, i) => <Metric key={i} label={p[0]} value={p[1]} unit={p[2]} glow={tab === 2} />)}
          </div>
        )}

        {tab === 1 && (
          <div className="perf-060">
            <div className="perf-timer-num">{timer.toFixed(2)}<span className="perf-timer-s">s</span></div>
            <div className="perf-timer-state">{running ? 'RUNNING\u2026' : timer > 0 ? 'COMPLETE' : 'READY'}</div>
            <div className="perf-timer-hint">Timer begins automatically above 1 MPH</div>
            <button className={`rl-btn pri perf-go${running ? ' running' : ''}`} onClick={startTimer}>
              {running ? 'STOP' : timer > 0 ? 'RUN AGAIN' : 'SIMULATE 0\u201360'}
            </button>
            {timer > 0 && !running && (
              <div className="perf-result">
                <span className="perf-result-lbl">0–60 TIME</span>
                <span className="perf-result-val">{timer.toFixed(2)} sec</span>
              </div>
            )}
          </div>
        )}

        {tab === 3 && (
          <div className="perf-hist">
            <div className="perf-hist-hdr"><span>RUN</span><span>TIME</span><span>DATE</span><span>MODE</span></div>
            {history.map((h, i) => (
              <div key={i} className="perf-hist-row"><span>{h.n}</span><span className="ph-time">{h.t}s</span><span>{h.d}</span><span>{h.m}</span></div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}

/* ── APPS SCREEN ── */

const apps = [
  ['M', 'Google Maps', 'drive', '#34a853'], ['W', 'Waze', 'drive', '#33ccff'], ['F', 'Fuelio', 'drive', '#f5a623'], ['T', 'Torque', 'drive', '#ff6d00'],
  ['Y', 'YouTube Music', 'media', '#ff0000'], ['S', 'Spotify', 'media', '#1db954'], ['P', 'Pandora', 'media', '#3668ff'], ['Y', 'YouTube', 'media', '#ff0000'],
  ['N', 'Netflix', 'media', '#e50914'], ['R', 'Radio', 'media', '#8b5cf6'],
  ['P', 'Phone', 'tools', '#34c759'], ['M', 'Messages', 'tools', '#5ac8fa'], ['C', 'Camera', 'tools', '#a3a3a3'],
  ['S', 'Settings', 'tools', '#8e8e93'], ['W', 'Weather', 'tools', '#0a84ff'], ['C', 'Calculator', 'tools', '#ff9500'],
  ['C', 'Calendar', 'tools', '#ff3b30'], ['C', 'Contacts', 'tools', '#ffcc00'], ['C', 'Clock', 'tools', '#1c1c1e'],
  ['F', 'Files', 'tools', '#007aff'], ['C', 'Chrome', 'tools', '#4285f4'], ['G', 'Gmail', 'tools', '#ea4335'],
  ['D', 'Drive', 'tools', '#fbbc04'], ['P', 'Photos', 'tools', '#ff5252'],
]

function AppsScreen() {
  const [tab, setTab] = useState(0)
  const [q, setQ] = useState('')
  const tabs = ['All', 'Driving', 'Media', 'Tools']
  const cat = [null, 'drive', 'media', 'tools'][tab]
  const filtered = apps.filter(a => (!cat || a[2] === cat) && a[1].toLowerCase().includes(q.toLowerCase()))

  return (
    <div className="rl-screen">
      <div className="rl-section-hdr apps-hdr">
        <div className="rl-section-badge">PRECISION APPS</div>
        <div className="rl-section-title">Every app, no clutter.</div>
      </div>
      <div className="rl-pad">
        <div className="rl-tabs">{tabs.map((x, i) => <Tab key={i} active={tab === i} onClick={() => setTab(i)}>{x}</Tab>)}</div>
        <input className="apps-search" placeholder="Search apps\u2026" value={q} onChange={e => setQ(e.target.value)} />
        <div className="apps-grid">
          {filtered.map((a, i) => (
            <button key={i} className="app-tile">
              <span className="app-tile-icon" style={{ background: a[3] }}>{a[0]}</span>
              <span className="app-tile-name">{a[1]}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}

/* ── SETTINGS ── */

const settingsMenuItems = [
  { name: 'Appearance', icon: '\u{1F3A8}', desc: 'Theme, wallpaper, brightness' },
  { name: 'Navigation', icon: '\u{1F4CD}', desc: 'Map style, voice, route prefs' },
  { name: 'Media', icon: '\u{1F3B5}', desc: 'Source, output, auto-resume' },
  { name: 'OBD-II / OBDLink MX+', icon: '\u{1F50C}', desc: 'Connection, PIDs, logging' },
  { name: 'Home & Work', icon: '\u2302', desc: 'Saved destinations' },
  { name: 'Quick Launch', icon: '\u26A1', desc: 'Customize shortcut bar' },
  { name: 'Audio', icon: '\u{1F3A7}', desc: 'Volume, EQ, sound modes' },
  { name: 'Permissions', icon: '\u{1F512}', desc: 'Manage app permissions' },
  { name: 'Backup & Reset', icon: '\u{1F4BE}', desc: 'Backup, restore, reset' },
  { name: 'About', icon: '\u2139', desc: 'Version, vehicle, support' },
]

function SettingsPanel({ name, settings, setSettings, onClose }) {
  const [homeAddr, setHomeAddr] = useState(settings.homeAddr || '123 Main St, Plainsboro, NJ')
  const [workAddr, setWorkAddr] = useState(settings.workAddr || '500 Tech Way, Princeton, NJ')
  const [ql, setQl] = useState(settings.ql || ['Navigation', 'Media', 'Phone', 'Performance'])
  const allQl = ['Navigation', 'Media', 'Phone', 'Performance', 'Weather', 'Camera', 'Settings', 'Apps']
  const flipQl = a => setQl(ql.includes(a) ? ql.filter(x => x !== a) : [...ql, a])
  const save = () => { setSettings({ ...settings, homeAddr, workAddr, ql }); onClose() }

  return (
    <div className="settings-panel">
      <div className="sp-hdr">
        <button className="sp-back" onClick={onClose}>{'\u2039'} BACK</button>
        <div className="sp-panel-title">{name}</div>
        <button className="sp-save" onClick={save}>SAVE</button>
      </div>
      <div className="sp-body">
        {name === 'Appearance' && (<>
          <div className="rl-card"><div className="rl-card-top">THEME</div><div className="theme-swatches">{['Red', 'Baja', 'Stealth', 'Blue', 'Orange'].map((t, i) => <button key={i} className={`theme-swatch${(settings.theme || 0) === i ? ' active' : ''}`} onClick={() => setSettings({ ...settings, theme: i })}><span className="theme-swatch-dot" style={{ background: ['#e1192d', '#d99a2b', '#aeb6c0', '#2b7de1', '#e16a1f'][i] }} /><span className="theme-swatch-name">{t}</span></button>)}</div></div>
          <div className="rl-card"><div className="rl-card-top">WALLPAPER</div><div className="rl-chip-row">{['TRX Hero', 'Carbon Fiber', 'Solid Dark', 'Desert'].map((w, i) => <Chip key={i} active={(settings.wallpaper || 0) === i} onClick={() => setSettings({ ...settings, wallpaper: i })}>{w}</Chip>)}</div></div>
          <div className="rl-card">
            <div className="rl-toggle-row"><span>Auto Brightness</span><Toggle on={settings.autoBright !== false} onClick={() => setSettings({ ...settings, autoBright: !settings.autoBright })} /></div>
            {settings.autoBright === false && <SliderRow icon={'\u2600'} min={0} max={100} value={settings.brightness || 80} onChange={v => setSettings({ ...settings, brightness: v })} display={`${settings.brightness || 80}%`} />}
          </div>
        </>)}
        {name === 'Navigation' && (<>
          <div className="rl-card"><div className="rl-card-top">MAP TYPE</div><div className="rl-chip-row">{['Default', 'Satellite', 'Terrain', 'Dark'].map((m, i) => <Chip key={i} active={(settings.navMapStyle || 0) === i} onClick={() => setSettings({ ...settings, navMapStyle: i })}>{m}</Chip>)}</div></div>
          <div className="rl-card"><div className="rl-card-top">ROUTE PREF</div><div className="rl-chip-row">{['Fastest', 'Shortest', 'No Tolls', 'No Highways'].map((r, i) => <Chip key={i} active={(settings.navRoutePref || 0) === i} onClick={() => setSettings({ ...settings, navRoutePref: i })}>{r}</Chip>)}</div></div>
          <div className="rl-card">
            <div className="rl-toggle-row"><span>Voice Guidance</span><Toggle on={settings.navVoice !== false} onClick={() => setSettings({ ...settings, navVoice: !settings.navVoice })} /></div>
            <div className="rl-toggle-row"><span>Speed Limit Alerts</span><Toggle on={settings.navSpeedAlert !== false} onClick={() => setSettings({ ...settings, navSpeedAlert: !settings.navSpeedAlert })} /></div>
            <div className="rl-toggle-row"><span>Traffic Layer</span><Toggle on={settings.navTraffic !== false} onClick={() => setSettings({ ...settings, navTraffic: !settings.navTraffic })} /></div>
          </div>
        </>)}
        {name === 'Media' && (<>
          <div className="rl-card"><div className="rl-card-top">DEFAULT SOURCE</div><div className="rl-chip-row">{['YouTube Music', 'Spotify', 'Pandora', 'Radio'].map((s, i) => <Chip key={i} active={(settings.mediaSrc || 0) === i} onClick={() => setSettings({ ...settings, mediaSrc: i })}>{s}</Chip>)}</div></div>
          <div className="rl-card"><div className="rl-card-top">OUTPUT</div><div className="rl-chip-row">{['Vehicle', 'Bluetooth', 'Phone'].map((o, i) => <Chip key={i} active={(settings.mediaOut || 0) === i} onClick={() => setSettings({ ...settings, mediaOut: i })}>{o}</Chip>)}</div></div>
          <div className="rl-card"><div className="rl-toggle-row"><span>Auto-resume on Connect</span><Toggle on={settings.mediaResume !== false} onClick={() => setSettings({ ...settings, mediaResume: !settings.mediaResume })} /></div></div>
        </>)}
        {name === 'OBD-II / OBDLink MX+' && (<>
          <div className="rl-card"><div className="rl-card-top">STATUS</div><div className="obd-status">{'\u2715'} DISCONNECTED — Pair via Bluetooth</div></div>
          <div className="rl-card"><div className="rl-card-top">UNITS</div><div className="rl-chip-row">{['Imperial (\u00b0F, mph)', 'Metric (\u00b0C, km/h)'].map((u, i) => <Chip key={i} active={(settings.obdUnits || 0) === i} onClick={() => setSettings({ ...settings, obdUnits: i })}>{u}</Chip>)}</div></div>
          <div className="rl-card">
            <div className="rl-toggle-row"><span>Data Logging</span><Toggle on={!!settings.obdLog} onClick={() => setSettings({ ...settings, obdLog: !settings.obdLog })} /></div>
            <div className="rl-toggle-row"><span>Auto-reconnect</span><Toggle on={settings.obdRecon !== false} onClick={() => setSettings({ ...settings, obdRecon: !settings.obdRecon })} /></div>
          </div>
          <div className="rl-card"><div className="rl-card-top">SUPPORTED PIDs</div><div className="pid-list">RPM · Coolant · Intake · Battery · Engine Load · Trans Temp · Throttle · MAF</div></div>
        </>)}
        {name === 'Home & Work' && (<>
          <div className="rl-card"><div className="rl-card-top">HOME ADDRESS</div><input className="rl-input" value={homeAddr} onChange={e => setHomeAddr(e.target.value)} /></div>
          <div className="rl-card"><div className="rl-card-top">WORK ADDRESS</div><input className="rl-input" value={workAddr} onChange={e => setWorkAddr(e.target.value)} /></div>
        </>)}
        {name === 'Quick Launch' && (
          <div className="rl-card"><div className="rl-card-top">SHORTCUT BAR</div><div className="ql-list">{allQl.map(a => <button key={a} className={`ql-item${ql.includes(a) ? ' on' : ''}`} onClick={() => flipQl(a)}><span>{a}</span><Toggle on={ql.includes(a)} onClick={() => flipQl(a)} /></button>)}</div></div>
        )}
        {name === 'Audio' && (<>
          <div className="rl-card"><div className="rl-card-top">VOLUME</div><SliderRow icon={'\u{1F508}'} min={0} max={100} value={settings.vol} onChange={v => setSettings({ ...settings, vol: v })} /></div>
          <div className="rl-card"><div className="rl-card-top">EQUALIZER</div>
            <SliderRow label="BASS" min={0} max={10} value={settings.bass} onChange={v => setSettings({ ...settings, bass: v })} display={`+${settings.bass}`} />
            <SliderRow label="TREBLE" min={0} max={10} value={settings.treble} onChange={v => setSettings({ ...settings, treble: v })} display={`+${settings.treble}`} />
            <SliderRow label="BAL" min={-10} max={10} value={settings.bal} onChange={v => setSettings({ ...settings, bal: v })} display={settings.bal > 0 ? `R+${settings.bal}` : settings.bal < 0 ? `L+${-settings.bal}` : 'CTR'} />
          </div>
          <div className="rl-card"><div className="rl-card-top">SOUND MODE</div><div className="rl-chip-row">{['Auto', 'Surround', 'Stereo', 'Party'].map((m, i) => <Chip key={i} active={(settings.soundMode || 0) === i} onClick={() => setSettings({ ...settings, soundMode: i })}>{m}</Chip>)}</div></div>
        </>)}
        {name === 'Permissions' && (
          <div className="rl-card"><div className="rl-card-top">GRANTED</div>{perms.map((p, i) => <div key={i} className="perm-row"><span className="perm-icon">{p.icon}</span><span className="perm-name">{p.title}</span><span className="perm-granted">GRANTED</span></div>)}</div>
        )}
        {name === 'Backup & Reset' && (<>
          <div className="rl-card"><div className="rl-toggle-row"><span>Auto-backup to Google Drive</span><Toggle on={!!settings.backup} onClick={() => setSettings({ ...settings, backup: !settings.backup })} /></div><button className="rl-btn" style={{ marginTop: 10 }}>BACK UP NOW</button></div>
          <div className="rl-card"><button className="rl-btn danger">RESET TO DEFAULTS</button></div>
        </>)}
        {name === 'About' && (
          <div className="rl-card"><div className="about-grid">{[['App', 'TRX Launcher'], ['Version', '6.0 Redline'], ['Build', '2026.09.19'], ['Vehicle', 'RAM 1500 TRX'], ['Display', 'Uconnect 5 (12\u2033)'], ['Engine', '6.2L SC V8'], ['Output', '702 HP / 650 lb-ft']].map((r, i) => <div key={i} className="about-row"><span>{r[0]}</span><span>{r[1]}</span></div>)}</div></div>
        )}
      </div>
    </div>
  )
}

/* ── MAIN APP ── */

const navItems = [
  { icon: '\u2302', label: 'HOME' }, { icon: '\u27A4', label: 'NAV' },
  { icon: '\u266B', label: 'MEDIA' }, { icon: '\u25F4', label: 'PERF' }, { icon: '\u25A6', label: 'APPS' },
]

const themeNames = ['red', 'baja', 'stealth', 'blue', 'orange']

const defaults = {
  theme: 0, wallpaper: 0, autoBright: true, brightness: 80,
  navMapStyle: 0, navRoutePref: 0, navVoice: true, navSpeedAlert: true, navTraffic: true, navSat: false, navNight: true, navUnits: 0,
  mediaSrc: 0, mediaOut: 0, mediaResume: true,
  vol: 45, bass: 5, treble: 3, bal: 0, soundMode: 0,
  obdUnits: 0, obdLog: false, obdRecon: true,
  homeAddr: '123 Main St, Plainsboro, NJ', workAddr: '500 Tech Way, Princeton, NJ',
  ql: ['Navigation', 'Media', 'Phone', 'Performance'],
}

export default function App() {
  const [page, setPage] = useState(0)
  const [booted, setBooted] = useState(false)
  const [showSettings, setShowSettings] = useState(false)
  const [settingsPanel, setSettingsPanel] = useState(null)
  const [settings, setSettings] = useState(defaults)
  const [quickApps, setQuickApps] = useState(() => {
    try { return JSON.parse(localStorage.getItem('trx-quick-apps')) || ['Google Maps', 'Phone', 'Spotify', 'Weather'] } catch { return ['Google Maps', 'Phone', 'Spotify', 'Weather'] }
  })

  useEffect(() => {
    try { localStorage.setItem('trx-quick-apps', JSON.stringify(quickApps)) } catch {}
  }, [quickApps])

  if (!booted) return <div className="rl-frame"><Splash onDone={() => setBooted(true)} /></div>

  return (
    <div className="rl-wrapper">
      <div className="rl-frame" data-theme={themeNames[settings.theme || 0]}>
        <div className="rl-bezel">
          {/* Status bar */}
          <div className="rl-status">
            <div className="rl-status-l">
              <span className="rl-si">{'\u{1F4F6}'} LTE</span>
              <span className="rl-si">{'\u{1F4F1}'}</span>
            </div>
            <div className="rl-status-c">
              <span className="rl-brand-r">RAM</span>
              <span className="rl-brand-sep">•</span>
              <span className="rl-brand-w">TRX LAUNCHER</span>
            </div>
            <div className="rl-status-r">
              <span className="rl-si">67°</span>
              <span className="rl-si"><Clock /></span>
              <button className="rl-gear" onClick={() => { setShowSettings(true); setSettingsPanel(null) }}>{'\u2699'}</button>
            </div>
          </div>

          {/* Stage */}
          <div className="rl-stage">
            {page === 0 && <HomeScreen goTo={setPage} quickApps={quickApps} setQuickApps={setQuickApps} />}
            {page === 1 && <NavScreen settings={settings} setSettings={setSettings} goTo={setPage} />}
            {page === 2 && <MediaScreen settings={settings} setSettings={setSettings} />}
            {page === 3 && <PerfScreen settings={settings} setSettings={setSettings} />}
            {page === 4 && <AppsScreen />}
          </div>

          {/* Settings overlay */}
          {showSettings && (
            <div className="rl-settings-overlay">
              {settingsPanel ? (
                <SettingsPanel name={settingsPanel} settings={settings} setSettings={setSettings} onClose={() => setSettingsPanel(null)} />
              ) : (
                <div className="rl-settings-menu">
                  <div className="rl-settings-hdr">
                    <div className="rl-settings-title">SETTINGS</div>
                    <button className="rl-settings-close" onClick={() => setShowSettings(false)}>{'\u2715'}</button>
                  </div>
                  <div className="rl-settings-list">
                    {settingsMenuItems.map((s, i) => (
                      <button key={i} className="rl-settings-item" onClick={() => setSettingsPanel(s.name)}>
                        <span className="rl-si-icon">{s.icon}</span>
                        <span className="rl-si-info"><span className="rl-si-name">{s.name}</span><span className="rl-si-desc">{s.desc}</span></span>
                        <span className="rl-si-arrow">{'\u203A'}</span>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Bottom nav */}
          <div className="rl-nav">
            {navItems.map((n, i) => (
              <button key={i} className={`rl-nav-btn${page === i ? ' active' : ''}`} onClick={() => { setPage(i); setShowSettings(false) }}>
                <span className="rl-nav-icon">{n.icon}</span>
                <span className="rl-nav-lbl">{n.label}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
      <div className="rl-preview-label">TRX Launcher v6.0 Redline — RAM TRX Uconnect 5 Preview</div>
    </div>
  )
}
