import { ToastContainer } from "react-toastify";
import Submission from "./pages/Submission";
import 'react-toastify/dist/ReactToastify.css';
import Nav from "./components/Nav";
import Footer from "./components/Footer";
import Credits from "./pages/Credits";
import { Route, Routes } from "react-router-dom";

function App() {
  return (
    <>
      <ToastContainer
        position="bottom-center"
        draggable={false}
      />


      <Nav />

      <div className='container mx-auto'>
        <div className="duration-300 min-h-screen bg-white">
          <main className="max-w-11xl mx-auto mb-8 lg:mb-20 px-4 sm:px-6 lg:px-8">
            <div className="w-full pt-28">
              <Routes>
                <Route path="/" element={<Submission />} />
                <Route path="/credits" element={<Credits />} />
              </Routes>
            </div>
          </main>
        </div>
      </div>
      <Footer />
    </>
  );
}

export default App;
