package tstack.hsf.provider;

import org.springframework.stereotype.Service;

@Service("petService")
public class PetServiceImpl implements PetService {
    @Override
    public String getPetNameById(Long id) {
        return "cookie";
    }
}