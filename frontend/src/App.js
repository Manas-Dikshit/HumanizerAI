import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  // State hooks to manage data and UI state
  const [inputText, setInputText] = useState('');
  const [outputText, setOutputText] = useState('');
  const [level, setLevel] = useState('standard'); // New state for level
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Function to handle the conversion request
  const handleHumanize = async () => {
    // Basic validation
    if (!inputText.trim()) return;

    setIsLoading(true);
    setError('');
    setOutputText('');

    try {
      // Axios POST request to our Spring Boot backend
      const response = await axios.post('http://localhost:8081/api/humanize-text', {
        text: inputText,
        level: level
      });
      
      // Update output with the response data
      setOutputText(response.data.humanized); 
    } catch (err) {
      console.error(err);
      setError('Failed to connect to the server. Is the backend running?');
    } finally {
      // Stop loading spinner regardless of success or failure
      setIsLoading(false);
    }
  };

  return (
    <div className="container">
      <header className="header">
        <h1>Humanizer AI</h1>
        <p className="subtitle">Transform robotic text into natural human language</p>
      </header>

      <main className="main-content">
        <div className="input-section">
          <label htmlFor="ai-input">Paste AI Text:</label>
          <textarea
            id="ai-input"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            placeholder="Paste your ChatGPT or AI text here..."
            className="text-area input-area"
          />
        </div>

        <div className="controls-row">
           <div className="toggle-group">
            <label>Tone:</label>
            <select value={level} onChange={(e) => setLevel(e.target.value)} className="level-select">
                <option value="standard">Standard</option>
                <option value="casual">Casual</option>
                <option value="formal">Formal</option>
                <option value="undetectable">Ghost Mode 👻 (Undetectable)</option>
            </select>
           </div>
          <button 
            onClick={handleHumanize} 
            disabled={isLoading || !inputText}
            className="convert-btn"
          >
            {isLoading ? 'Humanizing...' : 'Humanize Text ✨'}
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}

        <div className="output-section">
          <label>Humanized Result:</label>
          <div className={`text-area output-area ${isLoading ? 'loading' : ''}`}>
             {outputText || (isLoading ? 'Processing...' : 'Result will appear here...')}
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
