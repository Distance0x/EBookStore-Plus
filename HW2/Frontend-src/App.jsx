import AppRouter from './router'
import './App.css'
import UserProvider from '/src/utils/context';

function App() {
  return (
    <UserProvider>
      <div className="APP">
        <AppRouter />
      </div>
    </UserProvider>
    
  )
}

export default App
